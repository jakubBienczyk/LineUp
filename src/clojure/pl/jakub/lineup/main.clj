(ns pl.jakub.lineup.main
  (:require [neko.activity :refer [defactivity set-content-view!]]
            [neko.debug :refer [*a]]
            [neko.notify :refer [toast]]
            [neko.ui :refer [config]]
            [neko.resource :as res]
            [neko.listeners.view :as view-listeners]
            [neko.find-view :refer [find-view]]
            [neko.threading :refer [on-ui]])
  (:import (android.widget Button ImageView)
           (android.app Activity)
           (android.content Context Intent)
           (android.view Window)))

;; We execute this function to import all subclasses of R class. This gives us
;; access to all application resources.
(res/import-all)

(declare add-event)

(def board (atom {0 [] 1 [] 2 [] 3 [] 4 [] 5 [] 6 []}))
(def human 1)
(def cpu 0)
(def nr_columns 7)
(def column_height 6)
(def game-on? (atom true))

(defn msg [activity msg]
  (toast activity msg :short))

(defn swap-board
  "add player's circle to a column"
  [col player]
  (swap! board (fn [actual_board] (merge-with conj actual_board {col player}))))

(defn find-by-name
  "find res by name"
  [name ^Activity activity]
  (.findViewById activity (.getIdentifier (.getResources ^Context (.getApplicationContext activity)) name "id" "pl.jakub.lineup")))

(defn finish-game
  "function to finish game and toast a score"
  ([player activity]
   (if (= player cpu)
     (msg activity "you lose!")
     (msg activity "you win!"))
   (swap! game-on? (fn [game] (not game))))
  ([activity]
   (msg activity "game over")
   (swap! game-on? (fn [game] (not game)))))

(defn paint
  "paint cricles with specified color"
  [col color ^Activity activity]
  (let [paint_with #(.setImageResource ^ImageView (find-by-name (str "col" col "row" (- (- column_height 1) (count (@board col)))) activity) %)]
    (if (= color "green")
      (paint_with R$drawable/green_circle)
      (paint_with R$drawable/red_circle))))

(defn check-board-horizontally [col board player]
  (let [height (- (count (board col)) 1)
        row (map #(get (second %) height) board)
        divide_row (fn [s e] (apply = player (subvec (into [] row) s e)))]
    (some identity (map divide_row (range (- nr_columns 3)) (range 4 (+ nr_columns 1))))))

(defn check-board-vertically [col board player]
  (if (> 4 (count (board col)))
    false
    (apply = player (take-last 4 (board col)))))

(defn check-board-diagonally [col board player direction]
  (let [height (- (count (board col)) 1)
        sub #(if (= direction "left") (- % col) (- col %))
        diagonal (map (fn [x] (get (board x) (- height (sub x)))) (range 7))
        divide_diagonal (fn [s e] (apply = player (subvec (into [] diagonal) s e)))]
    (some identity (map divide_diagonal (range (- nr_columns 3)) (range 4 (+ nr_columns 1))))))

(defn check-board-diagonally-right [col board player]
  (check-board-diagonally col board player "right"))

(defn check-board-diagonally-left [col board player]
  (check-board-diagonally col board player "left"))

(defn check-board
  "check if game is over"
  ([col board player]
   (let [check #(% col board player)]
     (or (check check-board-vertically)
         (check check-board-horizontally)
         (check check-board-diagonally-right)
         (check check-board-diagonally-left))))
  ([board player]
   (some identity (map #(check-board % board player) (range nr_columns)))))

(defn possible-moves [board]
  "return all possible moves"
  (map first (filter #(> column_height (count (second %))) board)))

(defn player-can-win?
  "check if player has winning move"
  ([board player]
   (let [boards_with_next_move (map #(merge-with conj board {% player}) (range nr_columns))]
     (some identity (map #(check-board %1 %2 player) (range nr_columns) boards_with_next_move))))
  ([board col player]
   (check-board col (merge-with conj board {col player}) player)))

(defn player-can-win
  "check if player can win and return column with winnig move (or -1)"
  ([board possible_moves player]
   (if (empty? possible_moves)
     -1
     (let [col (first possible_moves)]
       (if (check-board col (merge-with conj board {col player}) player)
         col
         (player-can-win board (rest possible_moves) player)))))
  ([board player]
   (player-can-win board (possible-moves board) player)))

(defn player-can-make-winning-three
  "check if player can make great move (with three succ circels and two moves available)"
  ([board player moves]
   (if (empty? moves)
     -1
     (let [move (first moves)]
       (if (and (check-board move (merge-with conj board {move player} {(+ 3 move) player}) player)
                (not (player-can-win? (merge-with conj board {move cpu}) human)))
         move
         (player-can-make-winning-three board player (rest moves))))))
  ([board player]
   (let [possible_moves (into [] (possible-moves board))]
     (player-can-make-winning-three board player (filter #(contains? possible_moves (+ 3 %)) possible_moves)))))

(defn check-winning-positions
  "check if cpu can make/block winning position"
  ([board possible_moves player]
   (if (empty? possible_moves)
     -1
     (let [col (first possible_moves)
           new-board #(merge-with conj board {col %})]
       (if (and (player-can-win? (new-board player) player) (not (player-can-win? (new-board cpu) human)))
         col
         (check-winning-positions board (rest possible_moves) player)))))
  ([board player]
   (check-winning-positions board (possible-moves board) player)))

(defn make-random-move
  "filter all position to get best ones"
  ([board possible_moves result]
   (if (empty? possible_moves)
     result
     (let [col (first possible_moves)
           new-board (merge-with conj board {col cpu})]
       (if (not (player-can-win? new-board human))
         (make-random-move board (rest possible_moves) (conj result col))
         (make-random-move board (rest possible_moves) result)))))
  ([board]
   (make-random-move board (possible-moves board) [])))

(defn make-cpu-move
  [col activity]
  (do
    (paint col "red" activity)
    (swap-board col cpu)
    (if (check-board col @board cpu)
      (finish-game cpu activity)
      (if (apply = (conj (map #(count (@board %)) (range nr_columns)) column_height))
        (finish-game activity)))))

(defn computer-move
  "make cpu move"
  [board activity]
  ; check if cpu can win
  (let [move (player-can-win board cpu)]
    (if (<= 0 move)
      (make-cpu-move move activity)
      ; check if human can win
      (let [move (player-can-win board human)]
        (if (<= 0 move)
          (make-cpu-move move activity)
          ; check if human can make winning three
          (let [move (player-can-make-winning-three board human)]
            (if (<= 0 move)
              (make-cpu-move move activity)
              ; check if cpu can make winning three
              (let [move (player-can-make-winning-three board cpu)]
                (if (<= 0 move)
                  (make-cpu-move move activity)
                  ; check if human can make winning position
                  (let [move (check-winning-positions board human)]
                    (if (<= 0 move)
                      (make-cpu-move move activity)
                      ; check if cpu can make winning position
                      (let [move (check-winning-positions board cpu)]
                        (if (<= 0 move)
                          (make-cpu-move move activity)
                          ; make good random move
                          (let [moves (make-random-move board)]
                            (if (not (empty? moves))
                              (make-cpu-move (rand-nth moves) activity)
                              ;make random move
                              (let [move (rand-nth (possible-moves board))]
                                (make-cpu-move move activity)))))))))))))))))


(defn restart
  "restart activity"
  [^Activity activity]
  (swap! board (fn [_] {0 [] 1 [] 2 [] 3 [] 4 [] 5 [] 6 []}))
  (swap! game-on? (fn [_] true))
  (.recreate activity))

(defn onClick
  "action on button click"
  [^Activity activity view]
  (if @game-on?
    (let [col (- (read-string (.getText ^Button view)) 1)]
      (if (= column_height (count (@board col)))
        (toast activity "full!" :short)
        (do
          (paint col "green" activity)
          (swap-board col human)
          (if (check-board col @board human)
            (finish-game human activity)
            (computer-move @board activity)))))
    (restart activity)))

; define activity and set click listeners
(defactivity pl.jakub.lineup.MyActivity
             :key :main

             (onCreate [this bundle]
                       (.superOnCreate this bundle)
                       (neko.debug/keep-screen-on this)
                       (on-ui
                         (set-content-view! (*a) R$layout/gamescreen)
                         (.setOnClickListener ^Button (.findViewById this R$id/col0) (view-listeners/on-click-call #(onClick this %)))
                         (.setOnClickListener ^Button (.findViewById this R$id/col1) (view-listeners/on-click-call #(onClick this %)))
                         (.setOnClickListener ^Button (.findViewById this R$id/col2) (view-listeners/on-click-call #(onClick this %)))
                         (.setOnClickListener ^Button (.findViewById this R$id/col3) (view-listeners/on-click-call #(onClick this %)))
                         (.setOnClickListener ^Button (.findViewById this R$id/col4) (view-listeners/on-click-call #(onClick this %)))
                         (.setOnClickListener ^Button (.findViewById this R$id/col5) (view-listeners/on-click-call #(onClick this %)))
                         (.setOnClickListener ^Button (.findViewById this R$id/col6) (view-listeners/on-click-call #(onClick this %))))))
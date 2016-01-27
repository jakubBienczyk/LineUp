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
           (android.content Context Intent)))

;; We execute this function to import all subclasses of R class. This gives us
;; access to all application resources.
(res/import-all)

(declare add-event)

(def board (atom {0 [] 1 [] 2 [] 3 [] 4 [] 5 [] 6 []}))
(def player 1)
(def human 1)
(def cpu 0)
(def size 7)
(def game-on? (atom true))

(defn msg [activity msg]
  (toast activity msg :short))

(defn check-board-horizontally [col board player]
  (let [height (- (count (board col)) 1)
        row (map #(get (second %) height) board)
        divide_row (fn [s e] (apply = player (subvec (into [] row) s e)))]
    (some identity (map divide_row (range 4) (range 4 8)))))

(defn check-board-vertically [col board player]
  (if (> 4 (count (board col)))
    false
    (apply = player (take-last 4 (board col)))))

(defn check-board-diagonally-right [col board player]
  (let [height (- (count (board col)) 1)
        diagonal (map #(get (board %) (- height (- col %))) (range 7))
        divide_diagonal (fn [s e] (apply = player (subvec (into [] diagonal) s e)))]
    (some identity (map divide_diagonal (range 4) (range 4 8)))))

(defn check-board-diagonally-left [col board player]
  (let [height (- (count (board col)) 1)
        diagonal (map #(get (board %) (- height (- % col))) (range 7))
        divide_diagonal (fn [s e] (apply = player (subvec (into [] diagonal) s e)))]
    (some identity (map divide_diagonal (range 4) (range 4 8)))))

(defn check-board
  ([col board player]
   (let [check #(% col board player)]
     (or (check check-board-vertically) (check check-board-horizontally) (check check-board-diagonally-right) (check check-board-diagonally-left))))
  ([board player]
   (some identity (map #(check-board % board player) (range 7)))))

(defn cpu-can-win?
  ([board]
   (let [boards_with_next_cpu_move (map #(merge-with conj board {% 0}) (range 7))]
     (some identity (map #(check-board %1 %2 0) (range 8) boards_with_next_cpu_move))))
  ([board col]
   (check-board col (merge-with conj board {col 0}) 0)))

(defn possible-moves [board]
  (map first (filter #(> 6 (count (second %))) board)))

(defn swap-board
  [col player]
  (swap! board (fn [actual_board] (merge-with conj actual_board {col player}))))

(defn find-by-name
  [name ^Activity activity]
  (.findViewById activity (.getIdentifier (.getResources ^Context (.getApplicationContext activity)) name "id" "pl.jakub.lineup")))

(defn paint
  [col color ^Activity activity]
  (let [paint_with #(.setImageResource ^ImageView (find-by-name (str "col" col "row" (- 5 (count (@board col)))) activity) %)]
    (if (= color "green")
      (paint_with R$drawable/green_circle)
      (paint_with R$drawable/red_circle))))

(defn finish-game
  ([player activity]
   (if (= player cpu)
     (msg activity "you lose!")
     (msg activity "you win!"))
   (swap! game-on? (fn [game] (not game))))
  ([activity]
   (msg activity "game over")
   (swap! game-on? (fn [game] (not game)))))

(defn human-can-win?
  ([board]
   (let [boards_with_next_human_move (map #(merge-with conj board {% 1}) (range 7))]
     (some identity (map #(check-board %1 %2 1) (range 8) boards_with_next_human_move))))
  ([board col]
   (check-board col (merge-with conj board {col 1}) 1)))

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
  ([board player moves]
   (if (empty? moves)
     -1
     (let [move (first moves)]
       (if (and (check-board move (merge-with conj board {move player} {(+ 3 move) player}) player) (not (human-can-win? (merge-with conj board {move cpu}))))
         move
         (player-can-make-winning-three board player (rest moves))))))
  ([board player]
   (let [possible_moves (into [] (possible-moves board))]
     (player-can-make-winning-three board player (filter #(contains? possible_moves (+ 3 %)) possible_moves)))))

(defn block-human
  "check where should block human and return column number (or -1 if there is no need to block)"
  ([board possible_moves]
   (if (empty? possible_moves)
     -1
     (let [col (first possible_moves)
           new-board #(merge-with conj board {col %})]
       (if (and (human-can-win? (new-board human)) (not (human-can-win? (new-board cpu))))
         col
         (block-human board (rest possible_moves))))))
  ([board]
   (block-human board (possible-moves board))))

(defn make-winning-position
  "check if can make winning position and return column (or -1)"
  ([board possible_moves]
   (if (empty? possible_moves)
     -1
     (let [col (first possible_moves)
           new-board #(merge-with conj board {col %})]
       (if (and (cpu-can-win? (new-board cpu)) (not (human-can-win? (new-board cpu))))
         col
         (make-winning-position board (rest possible_moves))))))
  ([board]
   (make-winning-position board (possible-moves board))))

(defn make-cpu-move
  [col activity]
  (do
    (paint col "red" activity)
    (swap-board col cpu)
    (if (check-board col @board cpu)
      (finish-game cpu activity)
      (if (apply = (conj (map #(count (@board %)) (range 8)) 7))
        (finish-game activity)))))

(defn computer-move
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
                  (let [move (block-human board)]
                    (if (<= 0 move)
                      (make-cpu-move move activity)
                      ; check if cpu can make winning position
                      (let [move (make-winning-position board)]
                        (if (<= 0 move)
                          (make-cpu-move move activity)
                          ; make random move
                          (let [move (rand-nth (possible-moves board))]
                            (msg activity "los")
                            (make-cpu-move move activity)))))))))))))))

(defn restart
  "restart activity"
  ; TODO repair that
  [^Activity activity]
  (swap! board (fn [_] {0 [] 1 [] 2 [] 3 [] 4 [] 5 [] 6 []}))
  (swap! game-on? (fn [_] true))
  (.recreate activity))

(defn onClick
  "action on button click"
  [^Activity activity view]
  (if @game-on?
    (let [col (- (read-string (.getText ^Button view)) 1)]
      (if (= 6 (count (@board col)))
        (toast activity "full!" :short)
        (do
          (paint col "green" activity)
          (swap-board col player)
          (if (check-board col @board player)
            (finish-game player activity)
            (computer-move @board activity)))))
    (restart activity)))

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
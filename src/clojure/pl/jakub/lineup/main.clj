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
           (android.content Context)))

;; We execute this function to import all subclasses of R class. This gives us
;; access to all application resources.
(res/import-all)

(declare add-event)

(def board (atom {0 [] 1 [] 2 [] 3 [] 4 [] 5 [] 6 []}))

(defn check-board-horizontally [col player]
  (let [height      (- (count (@board col)) 1)
        row         (map #(get (second %) height) @board)
        divide_row  (fn [s e] (apply = player (subvec (into [] row) s e)))]
    (some identity (map divide_row (range 4) (range 4 8)))))

(defn check-board-vertically [col player]
  (if (> 4 (count (@board col)))
    false
    (apply = player (take-last 4 (@board col)))))

(defn check-board-diagonally-right [col player]
  (let [height    (- (count (@board col)) 1)
        diagonal  (map #(get (@board %) (- height (- col %))) (range 8))
        divide_diagonal  (fn [s e] (apply = player (subvec (into [] diagonal) s e)))]
    (some identity (map divide_diagonal (range 4) (range 4 8)))))

(defn check-board-diagonally-left [col player]
  (let [height    (- (count (@board col)) 1)
        diagonal  (map #(get (@board %) (- height (- % col))) (range 8))
        divide_diagonal  (fn [s e] (apply = player (subvec (into [] diagonal) s e)))]
    (some identity (map divide_diagonal (range 4) (range 4 8)))))

(defn check-board [col player]
  (or (check-board-vertically col player) (check-board-horizontally col player) (check-board-diagonally-right col player) (check-board-diagonally-left col player)))

(defn computer-move [] ())

(defn find-by-name
  [name ^Activity activity]
  (.findViewById activity (.getIdentifier (.getResources ^Context (.getApplicationContext activity)) name "id" "pl.jakub.lineup")))

(defn paint
  [col color ^Activity activity]
  (let [paint_with #(.setImageResource ^ImageView (find-by-name (str "col" col "row" (- 5 (count (@board col)))) activity) %)]
    (if (= color "green")
      (paint_with R$drawable/green_circle)
      (paint_with R$drawable/red_circle))))

(defn swap-board
  [col player]
  (swap! board (fn [actual_board] (merge-with conj actual_board {col player}))))

(defn onClick
  "action on button click"
  [^Activity activity view]
  (let [col (- (read-string (.getText ^Button view)) 1)]
    (paint col "green" activity)
    (swap-board col 1)
    (if (check-board col 1)
      (toast activity "you win!" :long)
      (toast activity "you lost!" :short))
    ))

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
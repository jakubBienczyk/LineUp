(ns pl.jakub.lineup.main
  (:require [neko.activity :refer [defactivity set-content-view!]]
            [neko.debug :refer [*a]]
            [neko.notify :refer [toast]]
            [neko.ui :refer [config]]
            [neko.resource :as res]
            [neko.listeners.view :as view-listeners]
            [neko.find-view :refer [find-view]]
            [neko.threading :refer [on-ui]])
  (:import (android.widget Button)))

;; We execute this function to import all subclasses of R class. This gives us
;; access to all application resources.
(res/import-all)


(defn onClick
  "action on button click"
  [view]
  (let )
  (.getText ^Button view)
  (.setText ^Button view "tak"))

(defactivity pl.jakub.lineup.MyActivity
  :key :main

  (onCreate [this bundle]
    (.superOnCreate this bundle)
    (neko.debug/keep-screen-on this)
    (on-ui
      (set-content-view! (*a) R$layout/gamescreen)
      (.setOnClickListener ^Button (.findViewById this R$id/col1) (view-listeners/on-click-call onClick))
      (.setOnClickListener ^Button (.findViewById this R$id/col2) (view-listeners/on-click-call onClick))
      (.setOnClickListener ^Button (.findViewById this R$id/col3) (view-listeners/on-click-call onClick))
      (.setOnClickListener ^Button (.findViewById this R$id/col4) (view-listeners/on-click-call onClick))
      (.setOnClickListener ^Button (.findViewById this R$id/col5) (view-listeners/on-click-call onClick))
      (.setOnClickListener ^Button (.findViewById this R$id/col6) (view-listeners/on-click-call onClick))
      (.setOnClickListener ^Button (.findViewById this R$id/col7) (view-listeners/on-click-call onClick)))))

(ns play-tesla.core
  (:require [play-cljs.core :as p]
            [play-tesla.mario :as m]
            [play-tesla.desk :as d]
            [play-tesla.coffee :as c]))

(enable-console-print!)

(defonce game (p/create-game 512 512))
(defonce state (atom {}))

(def collision-offset 60)
(def desks
  [{:id 1 :x 50 :y 120 :collision {:x 50 :y (+ 120 collision-offset) :w d/width :h 30}}
   {:id 2 :x 160 :y 120 :collision {:x 160 :y (+ 120 collision-offset) :w d/width :h 30}}
   {:id 3 :x 250 :y 220 :collision {:x 250 :y (+ 220 collision-offset) :w d/width :h 30}}
   {:id 4 :x 360 :y 220 :collision {:x 360 :y (+ 220 collision-offset) :w d/width :h 30}}
   {:id 5 :x 50 :y 320 :collision {:x 50 :y (+ 320 collision-offset) :w d/width :h 30}}
   {:id 6 :x 160 :y 320 :collision {:x 160 :y (+ 320 collision-offset) :w d/width :h 30}}])

(def walls
  [{:id :wall :x 0 :y 0 :collision {:x 0 :y 0 :w 512 :h 100}}
   {:id :left :x 0 :y 0 :collision {:x 0 :y 0 :w -10 :h 512}}
   {:id :right :x 0 :y 0 :collision {:x 512 :y 0 :w 10 :h 512}}
   {:id :bottom :x 0 :y 0 :collision {:x 0 :y 512 :w 512 :h 10}}])

(def coffee-station
  {:id :coffee :x 340 :y 34 :collision {:x 340 :y 34 :w c/width :h c/height}})

(defn draw-desk [{:keys [x y]}]
  [:div {:x x :y y :width d/width :height d/height}
   d/desk])

(defn desks-behind-mario [mario-y {:keys [y]}] (< (+ y d/height) mario-y))
(defn desks-infrontof-mario [mario-y {:keys [y]}] (> (+ y d/height) mario-y))

(def main-screen
  (reify p/Screen
    (on-show [this]
      (reset! state {:mario {:x 50 :y 250 :direction :right :energy 100}}))
    (on-hide [this])
    (on-render [this]
      (p/render game

                (let [mario (:mario @state)]
                  [[:fill {:color "lightblue"}
                    [:image {:name "office.png" :swidth 512 :sheight 512 :sx 0}]]

                   [:div (:collision coffee-station) c/coffee]
                   
                   (->> desks
                        (filter (partial desks-behind-mario (:y mario)))
                        (map draw-desk))

                   [:div {:x (- (:x mario) (/ m/width 2)) :y (- (:y mario) m/height) :width m/width :height m/height}
                    (:current mario)]

                   (->> desks
                        (filter (partial desks-infrontof-mario (:y mario)))
                        (map draw-desk))

                   [:text {:value (select-keys (:mario @state) [:energy]) :x 10 :y 490 :size 16 :font "Georgia" :style :italic}]
                   ]))
      (reset! state
              (-> @state
                  (m/move game)
                  (c/drink coffee-station)
                  (m/prevent-move desks)
                  (m/prevent-move walls)
                  (m/prevent-move [coffee-station])
                  (m/animate)
                  )))))

(doto game
  (p/start)
  (p/set-screen main-screen))


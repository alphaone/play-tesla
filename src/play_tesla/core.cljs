(ns play-tesla.core
  (:require [play-cljs.core :as p]
            [play-tesla.mario :as m]
            [play-tesla.desk :as d]
            [play-tesla.coffee :as c]))

(enable-console-print!)

(defonce game (p/create-game 512 512))
(defonce state (atom {}))

(def desk-collision-offset 60)

(def walls
  {:wall   {:x 0 :y 0 :collision {:x 0 :y 0 :w 512 :h 100}}
   :left   {:x 0 :y 0 :collision {:x 0 :y 0 :w -10 :h 512}}
   :right  {:x 0 :y 0 :collision {:x 512 :y 0 :w 10 :h 512}}
   :bottom {:x 0 :y 0 :collision {:x 0 :y 512 :w 512 :h 10}}})

(def coffee-station
  {:x 340 :y 34 :collision {:x 340 :y 34 :w c/width :h c/height}})

(defn draw-desk [[_ {:keys [x y] :as desk}]]
  [:div {:x x :y y :width d/width :height d/height}
   (d/draw-desk desk)])

(defn desks-behind-mario [mario-y [_ d]] (< (+ (:y d) d/height) mario-y))
(defn desks-infrontof-mario [mario-y [_ d]] (> (+ (:y d) d/height) mario-y))

(def main-screen
  (reify p/Screen
    (on-show [this]
      (reset! state {:mario {:x 50 :y 250 :direction :right :energy 100}
                     :desks {:desk-1 {:x          50 :y 120 :mode :fixed
                                      :activation {:x (+ 50 50) :y (+ 120 90) :w 10 :h 10}
                                      :collision  {:x 50 :y (+ 120 desk-collision-offset) :w d/width :h 30}}
                             :desk-2 {:x          160 :y 120 :mode :fixed
                                      :activation {:x (+ 50 160) :y (+ 120 90) :w 10 :h 10}
                                      :collision  {:x 160 :y (+ 120 desk-collision-offset) :w d/width :h 30}}
                             :desk-3 {:x          250 :y 220 :mode :fixed
                                      :activation {:x (+ 50 250) :y (+ 220 90) :w 10 :h 10}
                                      :collision  {:x 250 :y (+ 220 desk-collision-offset) :w d/width :h 30}}
                             :desk-4 {:x          360 :y 220 :mode :fixed
                                      :activation {:x (+ 50 360) :y (+ 220 90) :w 10 :h 10}
                                      :collision  {:x 360 :y (+ 220 desk-collision-offset) :w d/width :h 30}}
                             :desk-5 {:x          50 :y 320 :mode :fixed
                                      :activation {:x (+ 50 50) :y (+ 320 90) :w 10 :h 10}
                                      :collision  {:x 50 :y (+ 320 desk-collision-offset) :w d/width :h 30}}
                             :desk-6 {:x          160 :y 320 :mode :fixed
                                      :activation {:x (+ 50 160) :y (+ 320 90) :w 10 :h 10}
                                      :collision  {:x 160 :y (+ 320 desk-collision-offset) :w d/width :h 30}}}})
      
      (swap! state update-in [:timeoutid]
             (fn [_] (js/setInterval
                       (fn [] (swap! state d/break-test)) 5000))))
    
    (on-hide [this]
      (println "clear interval ..." (:timeoutid @state))
      (js/clearInterval (:timeoutid @state)))
    (on-render [this]
      (p/render game

                (let [mario (:mario @state)
                      desks (:desks @state)]
                  [[:fill {:color "lightblue"}
                    [:image {:name "office.png" :swidth 512 :sheight 512 :sx 0}]]

                   [:div (:collision coffee-station) c/coffee]

                   (->> desks
                        (filter (partial desks-behind-mario (:y mario)))
                        (map draw-desk))

                   [:div {:x (- (:x mario) (/ m/width 2)) :y (- (:y mario) m/height) :width m/width :height m/height}
                    (:current mario)
                    [:div {:y 58 :height 6}
                     (m/energy-chart mario)]]

                   (->> desks
                        (filter (partial desks-infrontof-mario (:y mario)))
                        (map draw-desk))

                   #_[:text {:value (select-keys (:mario @state) [:energy]) :x 10 :y 490 :size 16 :font "Georgia" :style :italic}]
                   ]))
      (reset! state
              (-> @state
                  (m/move game)
                  (c/drink coffee-station)
                  (d/fix-broken-tests)
                  (m/prevent-move (:desks @state))
                  (m/prevent-move walls)
                  (m/prevent-move {:coffee-station coffee-station})
                  (m/animate)
                  )))))

(doto game
  (p/start)
  (p/set-screen main-screen))



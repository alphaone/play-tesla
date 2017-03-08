(ns play-tesla.coffee
  (:require [play-tesla.utils :as u]
            [play-tesla.mario :as m]))

(def width 134)
(def height 92)

(def coffee [:image {:name "coffee.png" :swidth width :sheight height :sx 0}])

(defn near? [{:keys [x y]} coffee-station]
  (u/touching? x y m/width 20 :collision [:coffee-station coffee-station]))

(defn drink [{:keys [mario] :as state} coffee-station]
  (if (near? mario coffee-station)
    (assoc-in state [:mario :energy] 100)
    state))
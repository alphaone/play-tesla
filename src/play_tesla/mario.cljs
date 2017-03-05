(ns play-tesla.mario
  (:require [play-cljs.core :as p]
            [play-tesla.utils :as u]
            [play-tesla.desk :as d]))

(def width 32)
(def height 58)

(def stand-right [:image {:name "mario.png" :swidth width :sheight height :sx -2}])
(def walk-right-2 [:image {:name "mario.png" :swidth width :sheight height :sx 32}])
(def walk-right-3 [:image {:name "mario.png" :swidth width :sheight height :sx 68}])
(def walk-right [:animation {:duration 150} stand-right walk-right-2 walk-right-3 walk-right-2])

(def stand-left [:image {:name "mario.png" :swidth width :sheight height :sx 0 :flip-x true}])
(def walk-left-2 [:image {:name "mario.png" :swidth width :sheight height :sx 32 :flip-x true}])
(def walk-left-3 [:image {:name "mario.png" :swidth width :sheight height :sx 68 :flip-x true}])
(def walk-left [:animation {:duration 150} stand-left walk-left-2 walk-left-3 walk-left-2])

(def v-max 3)

(defn x-velocity [game old-velocity]
  (cond
    (contains? (p/get-pressed-keys game) 37) (* -1 v-max)
    (contains? (p/get-pressed-keys game) 39) v-max
    :else old-velocity))

(defn y-velocity [game old-velocity]
  (cond
    (contains? (p/get-pressed-keys game) 38) (* -1 v-max)
    (contains? (p/get-pressed-keys game) 40) v-max
    :else old-velocity))

(defn decelerate [velocity]
  (let [velocity (* velocity 0.9)]
    (if (< (Math/abs velocity) 0.1)
      0
      velocity)))

(defn move [{:keys [mario] :as state} game]
  (let [x (:x mario)
        y (:y mario)
        v-x (x-velocity game (:velocity-x mario))
        v-y (y-velocity game (:velocity-y mario))]
    (assoc state
      :mario (merge mario
                    {:last-x     x
                     :last-y     y
                     :x          (+ x v-x)
                     :y          (+ y v-y)
                     :velocity-x (decelerate v-x)
                     :velocity-y (decelerate v-y)}))))

(defn prevent-move [{:keys [mario] :as state} desks]
  (let [x (:x mario)
        y (:y mario)
        old-x (:last-x mario)
        old-y (:last-y mario)
        colliding (fn [x y] (keep (partial u/touching? x y width 10) desks))]
    (assoc state
      :mario (merge mario
                    (let [obsticals (colliding x old-y)]
                      (when (seq obsticals)
                        (prn "touching x" obsticals)
                        {:x old-x :velocity-x 0}))
                    (let [obsticals (colliding old-x y)]
                      (when (seq obsticals)
                        (prn "touching y" obsticals)
                        {:y old-y :velocity-y 0}))))))

(defn get-direction [{:keys [velocity-x direction]}]
  (cond (> velocity-x 0) :right
        (< velocity-x 0) :left
        :else direction))

(defn animate [{:keys [mario] :as state}]
  (let [direction (get-direction mario)
        v-x (Math/abs (:velocity-x mario))
        v-y (Math/abs (:velocity-y mario))
        moving? (or (> v-x 0.5) (> v-y 0.5))]
    (-> state
        (assoc-in [:mario :current] (cond
                                      moving? (if (= direction :right) walk-right walk-left)
                                      :else (if (= direction :right) stand-right stand-left)))
        (assoc-in [:mario :direction] direction))))

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

(defn velocity [game {:keys [velocity-x velocity-y energy]}]
  (let [v-max (-> energy (/ 100) (* 3) (+ 0.5))
        v-x (cond
              (contains? (p/get-pressed-keys game) 37) (* -1 v-max)
              (contains? (p/get-pressed-keys game) 39) v-max
              :else velocity-x)
        v-y (cond
              (contains? (p/get-pressed-keys game) 38) (* -1 v-max)
              (contains? (p/get-pressed-keys game) 40) v-max
              :else velocity-y)]
    [v-x v-y]))

(defn decelerate [velocity]
  (let [velocity (* velocity 0.9)]
    (if (< (Math/abs velocity) 0.1)
      0
      velocity)))

(defn decrease [old-energy]
  (let [energy (- old-energy 0.05)]
    (if (<= energy 0)
      0
      energy)))

(defn move [{:keys [mario] :as state} game]
  (let [x (:x mario)
        y (:y mario)
        [v-x v-y] (velocity game mario)
        energy (:energy mario)]
    (assoc state
      :mario (merge mario
                    {:last-x     x
                     :last-y     y
                     :x          (+ x v-x)
                     :y          (+ y v-y)
                     :velocity-x (decelerate v-x)
                     :velocity-y (decelerate v-y)
                     :energy     (decrease energy)}))))

(defn prevent-move [{:keys [mario] :as state} obstacles]
  (let [x (:x mario)
        y (:y mario)
        old-x (:last-x mario)
        old-y (:last-y mario)
        colliding (fn [x y] (keep (partial u/touching? x y width 10) obstacles))]
    (assoc state
      :mario (merge mario
                    (let [colliding-obstacles (colliding x old-y)]
                      (when (seq colliding-obstacles)
                        (println "touching x" colliding-obstacles)
                        {:x old-x :velocity-x 0}))
                    (let [colliding-obstacles (colliding old-x y)]
                      (when (seq colliding-obstacles)
                        (println "touching y" colliding-obstacles)
                        {:y old-y :velocity-y 0}))))))

(defn get-direction [{:keys [velocity-x direction]}]
  (cond (> velocity-x 0) :right
        (< velocity-x 0) :left
        :else direction))

(defn energy-color [energy]
  (cond
    (> energy 75) "#73ef0e"
    (> energy 50) "#d2ef0e"
    (> energy 25) "#efc20e"
    :else "#ef1e0e"))

(defn energy-chart [{:keys [energy]}]
  (let [color (energy-color energy)]
    [:fill {:color "gray"}
     [:rect {:x 0 :y 0 :width 32 :height 6}]
     [:stroke {:color color}
      [:fill {:color color}
       [:rect {:x 1 :y 1 :width (-> energy (/ 100) (* 30)) :height 4}]]]
     ]))

(defn animate [{:keys [mario] :as state}]
  (let [direction (get-direction mario)
        v-x (Math/abs (:velocity-x mario))
        v-y (Math/abs (:velocity-y mario))
        moving? (or (>= v-x 0.45) (>= v-y 0.45))]
    (-> state
        (assoc-in [:mario :current] (cond
                                      moving? (if (= direction :right) walk-right walk-left)
                                      :else (if (= direction :right) stand-right stand-left)))
        (assoc-in [:mario :direction] direction))))

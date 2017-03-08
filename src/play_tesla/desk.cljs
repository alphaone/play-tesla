(ns play-tesla.desk
  (:require [play-tesla.utils :as u]))

(def width 108)
(def height 90)

(defn draw-desk [{:keys [mode type]}]
  (let [sx-people-sprite (case type :a 0 :b 80 :c 160)
        x (if (= type :a) 55 10)
        sx-desk-sprite (if (= mode :broken) (* 2 width) 0)
        duration (case type :a 400 :b 500 :c 600)]
    [:div {}
     [:animation {:duration duration}
      [:image {:name "desk.png" :swidth width :sheight height :sx sx-desk-sprite}]
      [:image {:name "desk.png" :swidth width :sheight height :sx (+ width sx-desk-sprite)}]]
     [:div {:x x :y 15 :width 40 :height 80}
      [:animation {:duration duration}
       [:image {:name "people.png" :swidth 40 :sheight 80 :sx sx-people-sprite}]
       [:image {:name "people.png" :swidth 40 :sheight 80 :sx (+ 40 sx-people-sprite)}]]]]))

(defn fix-broken-tests [{:keys [mario desks] :as state}]
  (let [x (:x mario)
        y (:y mario)
        colliding (fn [x y] (keep (partial u/touching? x y width 10 :activation) desks))
        close-desks (colliding x y)]
    (if (seq close-desks)
      (do
        (prn "fixing" close-desks)
        (assoc-in state [:desks (first close-desks) :mode] :fixed))
      state)))

(defn fixed? [[_ desk]]
  (= :fixed (:mode desk)))

(defn break-test [{:keys [desks] :as state}]
  (let [fixed (filter fixed? desks)
        random-desk-id (->> fixed keys rand-nth)]
    (if random-desk-id
      (assoc-in state [:desks random-desk-id :mode] :broken)
      state)))

(ns play-tesla.utils)

(defn between? [v r-start r-end]
  (and (< r-start v) (< v r-end)))

(defn over-lapping? [x1-start x1-end y1-start y1-end x2-start x2-end y2-start y2-end]
  (and (or (between? x1-start x2-start x2-end)
           (between? x2-start x1-start x1-end)
           (between? x1-end x2-start x2-end)
           (between? x2-end x1-start x1-end))
       (or (between? y1-start y2-start y2-end)
           (between? y2-start y1-start y1-end)
           (between? y1-end y2-start y2-end)
           (between? y2-end y1-start y1-end))))

(defn touching? [mario-x mario-y width height [id obstacle]]
  (let [collision (:collision obstacle) 
        x-start (- mario-x (/ width 2))
        x-end (+ mario-x (/ width 2))
        y-start (- mario-y height)
        y-end mario-y

        o-x-start (:x collision)
        o-y-start (:y collision)
        o-x-end (+ o-x-start (:w collision))
        o-y-end (+ o-y-start (:h collision))]
    (when (over-lapping? x-start x-end y-start y-end o-x-start o-x-end o-y-start o-y-end)
      id)))

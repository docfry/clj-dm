(ns director-musices.score.glue
  (:require [director-musices.common-lisp.interpreter :as inr]
            [director-musices.common-lisp.glue :as glue]))

(defn load-active-score [string]
  (glue/eval-dm
    (str "(in-package :dm)
          (read-active-score-from-string \"" string "\")
          (init-music-score)")))

(defn load-active-score-from-file [path]
  (glue/eval-dm
    (str "(read-active-score-from-file \"" (inr/abcl-path path) "\")"
         "(init-music-score)")))

(defn load-active-score-from-midi-file [path]
  (glue/eval-dm (str "(load-midifile-fpath \"" (inr/abcl-path path) "\")"
                     "(init-music-score)")))

(defn get-active-score []
  (glue/eval-dm "(get-active-score)"))

(defn value->clj [v]
  (condp = v 
    'T true
    'NIL nil
    v))

(defn clj->value [v]
  (condp = v
    true 'T
    nil 'NIL
    v))

(defn segment->map [segment]
  (let [raw (.printObject segment)]
    (->> (.replaceAll raw " \\. " " ")
         read-string
         (map (fn [[k & vs]]
                [(keyword (.toLowerCase (str k)))
                 (if (== (count vs) 1)
                   (value->clj (first vs))
                   (map value->clj vs))]))
         (into {}))))

(defn map->segment [m]
  (let [raw (with-open [s (java.io.StringWriter.)]
              (binding [*out* s]
                (prn (map (fn [[k v]]
                            (concat (list (symbol (.toUpperCase (name k))))
                              (if (coll? v)
                                (map clj->value v)
                                (list (clj->value v))))) m))
                (str s)))]
    (.replaceAll raw "," "")))

(defn get-track [track-index]
  (let [raw (glue/eval-dm (str "(get-filtered-track " track-index ")"))]
    (->> raw
         .copyToArray
         (map segment->map))))

(defn get-segment [track-index segment-index]
  (nth (get-track track-index) segment-index nil))

(defn set-segment [track-index segment-index segment]
  (let [segment (if (map? segment)
                    (map->segment segment)
                    segment)]
    (glue/eval-dm (str "(setf (var-list (nth " segment-index 
                                     " (segment-list (nth " track-index 
                                                     " (track-list *active-score*))))) '"
                       segment ")"))))

(defn get-segment-parameter [track-index segment-index k]
  (get (get-segment track-index segment-index) (keyword (name k))))

(defn set-segment-parameter [track-index segment-index k v]
  (set-segment track-index segment-index
    (map->segment (assoc (get-segment track-index segment-index) 
                         (keyword (.toUpperCase (name k)))
                         v))))

(defn get-track-count []
  (.javaInstance (glue/eval-dm "(length (track-list *active-score*))")))

(defn get-track-property [id property]
  (.javaInstance (glue/eval-dm (str "(" property " (nth " id " (track-list *active-score*)))"))))

(defn set-track-property [id property value]
  (let [type (case property
               "trackname" :string
               "instrument-type" :string
               :native)
        value (case type
                :string (str "\"" value "\"")
                :native (str value))]
    (glue/eval-dm (str "(setf (" property " (nth " id " (track-list *active-score*))) " value ")"))))
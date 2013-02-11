(ns director-musices.score.ui
  (:use (director-musices.score
          [global :only [score-panel]])
        [clojure.java.io :only [resource]])
  (:require (director-musices.score
              [draw-score :as draw-score]
              [glue :as glue])
            [seesaw.core :as ssw]
            [seesaw.mig :as ssw-mig])
  )

(defn convert-track [track]
  (for [{:keys [dr ndr n] :as note} track]
    (assoc
      (if n
        (assoc note :pitch (first n)
          :length dr
          :nlength (second n)) ; (* 4 (second n)))
        note)
      :dr/ndr (- (/ dr ndr) 1))))

(def score-panel-reloader (atom nil))

(defn track-options-dialog [track-id]
  (let [p (ssw-mig/mig-panel)
        items
        (for [property ["trackname" "midi-channel"
                        "midi-initial-volume" "midi-initial-program"
                        "midi-bank-msb" "midi-bank-lsb"
                        "midi-pan" "midi-reverb" 
                        ;"synth" 
                        "instrument-type"
                        "track-delay"]]
          [[(ssw/label :text property)] [(ssw/text :text (str (glue/get-track-property track-id property))
                                                   :columns 15) 
                                         "wrap"]])]
    (ssw/config! p :items (reduce concat items))
    (-> (ssw/dialog :content p :option-type :ok-cancel
                    :success-fn (fn [_] 
                                  (doseq [item items]
                                    (glue/set-track-property track-id (.getText (ffirst item)) (.getText (first (second item)))))))
      ssw/pack!
      ssw/show!)
    ))

(defn score-view [id]
  (let [view (ssw-mig/mig-panel)
        sc (draw-score/score-component 
             (convert-track (glue/get-track id)) :clef \G )
        options-label (ssw/label :icon (resource "icons/gear_small.png"))
        graph-label   (ssw/label :icon (resource "icons/stats_small.png"))
        ]
    (ssw/listen sc 
                :mouse-clicked (fn [evt] (let [note-id (draw-score/get-note-for-x (.getX evt) sc)
                                               ta (ssw/text :text (-> (clojure.string/replace (str (glue/get-segment id note-id))
                                                                                              ", " "\n")
                                                                    (clojure.string/replace #"\{|\}" ""))
                                                            :multi-line? true)]
                                           (ssw/show! (ssw/dialog :content (ssw/scrollable ta) :option-type :ok-cancel :size [300 :by 300]
                                                                  :success-fn (fn [& _] (glue/set-segment id note-id (read-string (str "{" (.getText ta) "}")))))))))
    (ssw/listen options-label :mouse-clicked (fn [_] (track-options-dialog id)))
    (ssw/listen graph-label :mouse-clicked
                (fn [_]
                  (if-let [choice (ssw/input "what type?" :choices [:dr/ndr :sl :dr] :to-string #(subs (str %) 1))] ; note: not possible to use (name) here, since (name :dr/ndr) => "ndr"
                    (let [c (draw-score/score-graph-component choice sc :height 150)
                          remove-label (ssw/label :icon (resource "icons/stats_delete_small.png"))]
                      (ssw/listen remove-label :mouse-clicked
                                  (fn [_] 
                                    (.remove view remove-label)
                                    (.remove view c)
                                    (.revalidate view)))
                      (.add view remove-label)
                      (.add view c "span")
                      (.revalidate view)))))
    (ssw/config! view :items [[(ssw/vertical-panel :items [options-label graph-label])]
                              [sc "span"]])
    (add-watch score-panel-reloader (gensym) (fn [& _] (.setNotes sc (convert-track (glue/get-track id)))))
    {:score-component sc 
     :view sc
     ;:view view
     }))

(defn update-score-panel []
  (let [mouse-position-x-start (atom 0)
        initial-scale-x (atom 1)
        new-scale-x (atom 1)
        p (ssw-mig/mig-panel)
        score-views (for [i (range (glue/get-track-count))]
                      (let [sv (score-view i)
                            sc (:score-component sv)
                            ]
                        (ssw/listen sc 
                                    :mouse-pressed (fn [e] 
                                                     (reset! mouse-position-x-start (.getX e))
                                                     (reset! initial-scale-x (:scale-x @(.getOptionsAtom sc))))
                                    :mouse-dragged (fn [e] 
                                                     (reset! new-scale-x (* @initial-scale-x (/ (.getX e) @mouse-position-x-start)))))
                        (add-watch new-scale-x i (fn [_ _ _ scale-x] (.setScaleX sc scale-x)))
                        [(:view sv) "span"]
                        ;(:view sv)
                        ))]
    (ssw/config! p 
                 :items
                 (interleave score-views
                             (take (count score-views)
                                   (repeatedly #(vec [(ssw/separator :orientation :horizontal) "growx, span"]))))
                 )
    (ssw/config! score-panel :items [(ssw/scrollable p)])
    p))

(defn reload-ui [] ;(update-score-panel)
  )
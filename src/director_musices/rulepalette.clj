(ns director-musices.rulepalette
  (:use [director-musices
         [glue :only [apply-rules]]
         [score :only [reload-score-panel]]
         [utils :only [find-i new-file-dialog with-indeterminate-progress]]]
        [clojure.java.io :only [resource]])
  (:require [seesaw
             [core :as ssw]
             [chooser :as ssw-chooser]
             [mig :as ssw-mig]]))

;; loading

(defn string->rulepalette [string]
  (->> (load-string (str "(let [set-dm-var (fn [s content]
                         [(keyword s) content])
                         in-package (fn [_] )]
                         [" string "])"))
    (remove nil?)
    (into {})))

(defn path->rulepalette [path]
  (string->rulepalette (slurp path)))

;; Display

(def components-per-line 8)
(def slider-precision 1000)

(defn panel->rules [panel]
  (apply str
    (map (fn [[_ _ ch l _ k args _]]
           (if (.isSelected ch)
             (str \( (.getText l) " " 
                     (condp = (class k)
                       javax.swing.JSlider (double (/ (.getValue k) slider-precision))
                       nil) " " 
                     (.getText args) ")\n")))
      (partition components-per-line (.getComponents panel)))))

(defn get-line-index-starting-with [{:keys [rule-panel]} up]
  (/ 
    (find-i up (.getComponents rule-panel))
    components-per-line))

(defn get-rule-at [{:keys [rules]} line]
  (take components-per-line (drop (* line components-per-line) @rules)))

(defn add-rule-at [{:keys [rules]} line rule]
  (swap! rules (fn [coll]
                 (let [i (* components-per-line line)]
                   (concat (take i coll) rule (drop i coll))))))

(defn remove-rule-at [{:keys [rules]} line]
  (swap! rules (fn [coll]
                 (concat (take (* components-per-line line) coll)
                         (drop (* components-per-line (inc line)) coll)))))

(defn move-rule [rp-display line offset]
  (let [rule (get-rule-at rp-display line)]
    (remove-rule-at rp-display line)
    (add-rule-at rp-display (+ line offset) rule)))

(defn rule-name-dialog [& [previous-name previous-no-parameters?]]
  (let [tf (ssw/text :text previous-name :columns 20)
        cb (ssw/checkbox :text "Rule does not have any parameters"
                         :selected? previous-no-parameters?)]
    (if (ssw/show! (ssw/pack! (ssw/dialog :content (ssw/border-panel :center tf :south cb))))
      {:name (.getText tf)
       :no-parameters? (.isSelected cb)})))

(declare rule-display)

(defn rule-navigation [rp-display]
  (let [up (ssw/label :icon (resource "icons/up_alt.png"))
        down (ssw/label :icon (resource "icons/down_alt.png"))
        rmv (ssw/label :icon (resource "icons/delete.png"))]
    (ssw/listen up :mouse-clicked
      (fn [_] (move-rule rp-display (get-line-index-starting-with rp-display up) -1)))
    (ssw/listen down :mouse-clicked
      (fn [_] (move-rule rp-display (get-line-index-starting-with rp-display up) 1)))
    (ssw/listen rmv :mouse-clicked 
      (fn [_] (remove-rule-at rp-display (get-line-index-starting-with rp-display up))))
    [up down rmv]))

(defn rule-parameter-display [no-parameters? k args]
  (if no-parameters?
    ["" "" ""]
    (let [t (ssw/text :text k :columns 5)
          s (ssw/slider :min (* -5 slider-precision) :max (* 5 slider-precision) 
                        :value (* k slider-precision) :snap-to-ticks? false)]
      (.addActionListener t
        (reify java.awt.event.ActionListener
          (actionPerformed [_ _] (.setValue s (* (read-string (.getText t)) slider-precision)))))
      (.addChangeListener s
        (reify javax.swing.event.ChangeListener
          (stateChanged [_ _] (.setText t (str (double (/ (.getValue s) slider-precision)))))))
      [t s
       (ssw/text :text (apply str (interpose " " args)) :columns 30)])))

(defn rule-display* [rp-display [rule-name & [k & args]]]
  (let [[up down rmv] (rule-navigation rp-display)
        no-parameters? (not (number? k))]
    (concat 
      [up down
       (ssw/checkbox :selected? true)
       (let [l (ssw/label :text (str rule-name))]
         (ssw/listen l :mouse-clicked 
                     (fn [e]
                       (if (== (.getClickCount e) 2)
                         (if-let [{nm :name no-p? :no-parameters?} (rule-name-dialog (.getText l) no-parameters?)]
                           (if no-p? 
                             (let [line (get-line-index-starting-with rp-display up)]
                               (remove-rule-at rp-display line)
                               (add-rule-at rp-display line (rule-display rp-display [nm 'T])))
                             (if no-parameters?
                               (let [line (get-line-index-starting-with rp-display up)]
                                 (remove-rule-at rp-display line)
                                 (add-rule-at rp-display line (rule-display rp-display [nm 0.0])))
                               (.setText l nm)))))))
         l)]
      (rule-parameter-display no-parameters? k args)
      [rmv])))

(defn rule-display [rp-display rule]
  (let [rules (remove nil? (rule-display* rp-display rule))]
    (concat (map vector (butlast rules)) [[(last rules) "wrap"]])))

(defn set-editable [panel editable?]
  (doseq [cs (partition components-per-line (.getComponents panel))]
    (let [visibility (if editable? true false)]
      (.setVisible (first cs) visibility)
      (.setVisible (second cs) visibility)
      (.setVisible (last cs) visibility))))

(defn rulepalette-window [rulepalette]
  (let [rules (atom [])
        rule-panel (ssw-mig/mig-panel :constraints ["gap 1 1, novisualpadding" "" ""])
        editable? (ssw/checkbox :text "editable?")
        rp-display {;:syncrule-field syncrule-field
                    :syncrule (atom "melodic-sync")
                    :rules rules
                    :rule-panel rule-panel
                    ;:rulepalette-panel rulepalette-panel
                    }
        add-new-rule (ssw/action 
                       :icon (resource "icons/add.png")
                       :handler (fn [_] 
                                  (when-let [{nm :name np? :no-parameters?} (rule-name-dialog "" false)]
                                    (add-rule-at rp-display 0 (rule-display rp-display [nm (if np? 'T 0.0)]))
                                    (set-editable rule-panel (.isSelected editable?)))))
;        ifr (javax.swing.JInternalFrame. "" true true true true)
        menu (ssw/menubar :items
                          [(ssw/menu :text "file" :items 
                                     [(ssw/action :name "save rulepalette" :handler 
                                                  (fn [_]
                                                    (if-let [f (new-file-dialog rule-panel)]
                                                      (spit f
                                                            (str "(in-package \"DM\")\n(set-dm-var 'all-rules '(\n"
                                                                 (panel->rules rule-panel)
                                                                 "))\n(set-dm-var 'sync-rule-list '((NO-SYNC NIL) (MELODIC-SYNC T)))")))))])
                           (ssw/menu :text "apply" :items 
                                     [(ssw/action :name "apply"
                                                  :handler (fn [_]
                                                             (with-indeterminate-progress "applying rules"
                                                                                          (apply-rules (panel->rules rule-panel) @(:syncrule rp-display))
                                                                                          (reload-score-panel))))
                                      (ssw/action :name "reset and apply")])
                           (ssw/menu :text "config" :items 
                                     [editable?
                                      (ssw/action :name "Sync rule"
                                                  :handler (fn [_]
                                                             (if-let [in (ssw/input "Choose sync rule" :choices ["melodic-sync" "no-sync" "bar-sync"])]
                                                               (reset! (:syncrule rp-display) in))))])])]
    (add-watch rules "panel updater" (fn [_ _ old-items items]
      (ssw/config! rule-panel :items (concat items [[add-new-rule "span"]]))
;      (if (not (== (count old-items) (count items)))
 ;       (.pack ifr)
;        )
                                       ))
    (reset! rules (apply concat (map (partial rule-display rp-display) (:all-rules rulepalette))))
    (ssw/listen editable? :selection (fn [& _] (set-editable rule-panel (.isSelected editable?))))
    (set-editable rule-panel false)
;    (.setContentPane ifr rule-panel)
;    (.setResizable ifr false)
    {:content (ssw/border-panel :north menu :center rule-panel)}))

;; actions

(defn choose-and-open-rulepalette [& _]
  (ssw-chooser/choose-file :success-fn 
    (fn [_ f] (assoc (rulepalette-window (path->rulepalette (.getCanonicalPath f)))
                     :title (.getName f) ))))


(ns director-musices.rulepalette
  (:use [director-musices
         [glue :only [apply-rules]]
         [score :only [reload-score-panel]]
         [utils :only [find-i new-file-dialog with-indeterminate-progress]]
         [player :only [update-player]]]
        [clojure.java.io :only [resource]])
  (:require (director-musices
              [utils :as util])
            [seesaw
             [core :as ssw]
             [chooser :as ssw-chooser]
             [mig :as ssw-mig]]))

;; Default

(def default-rulepalette
"(in-package \"DM\")
(set-dm-var 'all-rules '(
(HIGH-LOUD 1.0)
(MELODIC-CHARGE 1.0 :AMP 1 :DUR 1 :VIBAMP 1)
(HARMONIC-CHARGE 1.0 :AMP 1 :DUR 1 :VIBFREQ 1)
(DURATION-CONTRAST 1.0 :AMP 1 :DUR 1)
(DOUBLE-DURATION 1.0)
(PUNCTUATION 1.1 :DUR 1 :DUROFF 1 :MARKPHLEVEL7 NIL)
(PHRASE-ARCH 1.5 :PHLEVEL 5 :TURN 0.3 :NEXT 1.3 :AMP 2)
(PHRASE-ARCH 1.5 :PHLEVEL 6 :TURN 2 :AMP 2 :LAST 0.2)
(NORMALIZE-SL T)
(NORMALIZE-DR T)
(FINAL-RITARD 1.0)
))
(set-dm-var 'sync-rule-list '((NO-SYNC NIL) (MELODIC-SYNC T)))")

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

(def rulepalettes (atom []))
(def rulepalette-container (ssw/tabbed-panel))

(declare choose-and-open-rulepalette)
(let [l (ssw/label "No rulepalette loaded yet, click here to load one!")
      p (util/centered-component l)
      r-cont-loaded? (atom false)]
  (ssw/listen p :mouse-clicked (fn [e] (choose-and-open-rulepalette)))
  
  (def rulepalette-panel (ssw/horizontal-panel :items [p]))
  
  (defn load-rulepalette-container []
    (when (not @r-cont-loaded?)
      (ssw/config! rulepalette-panel :items [rulepalette-container])
      (swap! r-cont-loaded? not))))

(defn add-rulepalette [c]
  (load-rulepalette-container)
  (swap! rulepalettes conj c)
  (ssw/config! rulepalette-container :tabs @rulepalettes))

(def components-per-line 8)
(def slider-precision 1000)

(defn panel->rules [panel]
  (apply str
         (map (fn [[_ _ ch l _ k args _ :as in-vec]]
                (if (.isSelected ch)
                  (str "(" (.getText l) " " 
                       (if (instance? javax.swing.JSlider k)
                         (double (/ (.getValue k) slider-precision)))
                       " " 
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

(defn move-rule [rp-obj line offset]
  (let [rule (get-rule-at rp-obj line)]
    (remove-rule-at rp-obj line)
    (add-rule-at rp-obj (+ line offset) rule)))

(defn rule-name-dialog [& [previous-name previous-no-parameters?]]
  (let [tf (ssw/text :text previous-name :columns 20)
        cb (ssw/checkbox :text "Rule does not have any parameters"
                         :selected? previous-no-parameters?)]
    (if (ssw/show! (ssw/pack! (ssw/dialog :content (ssw/border-panel :center tf :south cb))))
      {:name (.getText tf)
       :no-parameters? (.isSelected cb)})))

(declare rule-display)

(defn rule-navigation [rp-obj]
  (let [up (ssw/label :icon (resource "icons/up_alt.png"))
        down (ssw/label :icon (resource "icons/down_alt.png"))
        rmv (ssw/label :icon (resource "icons/delete.png"))]
    (ssw/listen up :mouse-clicked
      (fn [_] (move-rule rp-obj (get-line-index-starting-with rp-obj up) -1)))
    (ssw/listen down :mouse-clicked
      (fn [_] (move-rule rp-obj (get-line-index-starting-with rp-obj up) 1)))
    (ssw/listen rmv :mouse-clicked 
      (fn [_] (remove-rule-at rp-obj (get-line-index-starting-with rp-obj up))))
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

(defn rule-display* [rp-obj [rule-name & [k & args]]]
  (let [[up down rmv] (rule-navigation rp-obj)
        no-parameters? (not (number? k))]
    (concat 
      [up down
       (ssw/checkbox :selected? true)
       (let [l (ssw/label :text (str rule-name))]
         (ssw/listen l :mouse-clicked 
                     (fn [e]
                       (if (== (.getClickCount e) 2)
                         (if-let [{nm :name np? :no-parameters?} (rule-name-dialog (.getText l) no-parameters?)]
                           (if np? 
                             (let [line (get-line-index-starting-with rp-obj up)]
                               (remove-rule-at rp-obj line)
                               (add-rule-at rp-obj line (rule-display rp-obj [nm 'T])))
                             (if no-parameters?
                               (let [line (get-line-index-starting-with rp-obj up)]
                                 (remove-rule-at rp-obj line)
                                 (add-rule-at rp-obj line (rule-display rp-obj [nm 0.0])))
                               (.setText l nm)))))))
         l)]
      (rule-parameter-display no-parameters? k args)
      [rmv])))

(defn rule-display [rp-obj rule]
  (let [rules (remove nil? (rule-display* rp-obj rule))]
    (concat (map vector (butlast rules)) [[(last rules) "wrap"]])))

(defn set-editable [panel editable?]
  (doseq [cs (partition components-per-line (.getComponents panel))]
    (let [visibility (if editable? true false)]
      (.setVisible (first cs) visibility)
      (.setVisible (second cs) visibility)
      (.setVisible (last cs) visibility))))

(defn rulepalette-view [rulepalette]
  (let [rules (atom [])
        rule-panel (ssw-mig/mig-panel :constraints ["gap 1 1, novisualpadding" "" ""])
        editable? (ssw/checkbox :text "editable?")
        syncrule-select (ssw/combobox :model ["melodic-sync" "no-sync" "bar-sync"])
        rule-interaction? (ssw/checkbox :text "rule interaction")
        rule-interaction-c (ssw/text :text 2 :columns 5)
        rule-interaction (ssw/horizontal-panel :items [rule-interaction? rule-interaction-c])
        rp-obj {:syncrule (atom "melodic-sync")
                :rule-interaction? (atom false)
                :rule-interaction-c (atom 2)
                :rules rules
                :rule-panel rule-panel
                :content (ssw/scrollable rule-panel)
                }
        add-new-rule (ssw/action 
                       :icon (resource "icons/add.png")
                       :handler (fn [_] 
                                  (when-let [{nm :name np? :no-parameters?} (rule-name-dialog "" false)]
                                    (add-rule-at rp-obj 0 (rule-display rp-obj [nm (if np? 'T 0.0)]))
                                    (set-editable rule-panel (.isSelected editable?)))))]
    (add-watch rules "panel updater" (fn [_ _ old-items items]
      (ssw/config! rule-panel :items (concat items [[add-new-rule "span"] [editable? "span"] [syncrule-select "span"] [rule-interaction "span"]]))))
;                                             [(ssw/horizontal-panel :items [add-new-rule editable? syncrule-select rule-interaction]) "span"]]))))
    (reset! rules (apply concat (map (partial rule-display rp-obj) (:all-rules rulepalette))))
    (ssw/listen editable? :selection (fn [& _] (set-editable rule-panel (.isSelected editable?))))
    (ssw/listen syncrule-select :selection (fn [& _] (let [selection (.getSelectedItem syncrule-select)]
                                                       (reset! (:syncrule rp-obj) selection))))
    (ssw/listen rule-interaction? :selection (fn [& _] (reset! (:rule-interaction? rp-obj) (.isSelected rule-interaction?))))
    (ssw/listen rule-interaction-c :selection (fn [& _] (reset! (:rule-interaction-c rp-obj) (read-string (.getText rule-interaction-c)))))
    (set-editable rule-panel false)
    rp-obj))

;; actions

(def reset-on-apply (atom false))
(defn set-reset-on-apply [new-value]
  (swap! reset-on-apply (constantly new-value)))

(defn save-rp-obj [{:keys [rule-panel]}]
  (if-let [f (new-file-dialog rule-panel)]
    (spit f
          (str "(in-package \"DM\")\n(set-dm-var 'all-rules '(\n"
               (panel->rules rule-panel)
               "))\n(set-dm-var 'sync-rule-list '((NO-SYNC NIL) (MELODIC-SYNC T)))"))))

(defn apply-rp-obj [{:keys [rule-interaction? rule-interaction-c syncrule rule-panel]}]
  (with-indeterminate-progress "applying rules"
    (apply-rules (panel->rules rule-panel) @syncrule (if @rule-interaction? @rule-interaction-c))
    (reload-score-panel)))

(defn apply-current-rulepalette [& _]
  (let [rp-obj (nth @rulepalettes (.getSelectedIndex rulepalette-container) {})]
    (apply-rp-obj rp-obj))
  (update-player))

(defn apply-all-rulepalettes [& _]
  (doseq [rp-obj @rulepalettes]
    (apply-rp-obj rp-obj))
  (update-player))

(defn choose-and-open-rulepalette [& _]
  (ssw-chooser/choose-file :success-fn 
    (fn [_ f] 
      (add-rulepalette 
        (assoc (rulepalette-view (path->rulepalette (.getCanonicalPath f)))
               :title (.getName f))))))

(defn open-default-rulepalette [& _]
  (add-rulepalette (assoc (rulepalette-view (string->rulepalette default-rulepalette)) :title "Default")))


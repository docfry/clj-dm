(ns director-musices.util
  (:use [clojure.java.io :only [file]])
  (:require [seesaw 
             [core :as ssw]
             [chooser :as ssw-chooser]]
            [taoensso.timbre :as log]))

(defn find-i
  "Find the index of value in coll"
  [value coll]
  (let [limit (count coll)]
    (loop [i 0
           c coll]
      (if (== i limit)
        nil
        (if (= (first c) value)
          i
          (recur (inc i) (rest c)))))))

(defn new-file-dialog [& [parent]]
  (let [filename (ssw/text)
        dir (ssw/text :text (System/getProperty "user.home"))
        panel (ssw/border-panel
                :north filename :center dir
                :east (ssw/action
                        :name "browse"
                        :handler (fn [_]
                                   (ssw-chooser/choose-file
                                     parent :type "Ok" :selection-mode :dirs-only
                                     :success-fn (fn [_ f] (.setText dir (.getCanonicalPath f)))))))
        dialog (ssw/dialog :content panel :option-type :ok-cancel
                           :modal? true)]
    (if parent (.setLocationRelativeTo dialog parent))
    (.setResizable dialog false)
    (when (-> dialog ssw/pack! ssw/show!)
      (file (.getText dir) (.getText filename)))))

(defmacro with-indeterminate-progress [message & body]
  `(let [d# (ssw/frame :content (ssw/border-panel :north (ssw/label :text ~message :border 10)
                                                  :center (ssw/progress-bar :indeterminate? true :border 10)))]
     (ssw/pack! d#)
     (ssw/show! d#)
     ~@body
     (ssw/dispose! d#)))

(defn centered-component [c]
  (ssw/border-panel :center (ssw/horizontal-panel :items [:fill-h c :fill-h])))

(defmacro thread [& body]
  `(let [t# (Thread. (fn [] ~@body))]
     (.start t#)
     t#))

(defn tmp-dir []
  (if-let [path (System/getProperty "java.io.tmpdir")]
    (java.io.File. path)
    (java.io.File. ".")))

(defn watch-file [file on-change & [rate]]
  (let [rate (if rate rate 500)
        last-modified (atom (.lastModified file))
        task (proxy [java.util.TimerTask] []
               (run []
                    (let [new-last-modified (.lastModified file)]
                      (if (= 0 new-last-modified)
                        (log/error "i/o error when retreiving last modification of file.")
                        (when (> new-last-modified @last-modified)
                          (on-change)
                          (reset! last-modified new-last-modified))))))]
    (.scheduleAtFixedRate 
      (new java.util.Timer)
      task 0 rate)))

(defn start-panel [label-text items]
  (let [l (ssw/label :text label-text :h-text-position :left)]
    (.setAlignmentX l java.awt.Component/CENTER_ALIGNMENT)
    (.setFont l (.deriveFont (.getFont l) (float 15.0)))
    (centered-component
      (ssw/vertical-panel 
        :items [l [:fill-v 20]
                (ssw/horizontal-panel
                  :items items)]))))

(defn open-website [url]
  (.browse (java.awt.Desktop/getDesktop)
           (java.net.URI. url)))

(defn default-background []
  (javax.swing.UIManager/getColor "Panel.background"))

(defn configure-button-label [l on-click]
  (let [bg (.getBackground l)]
    (ssw/config! l :border 3)
    (ssw/listen l :mouse-clicked
                (fn [_] (on-click l)))
    (ssw/listen l :mouse-entered
                (fn [_] (ssw/config! l :background "#AAA")))
    (ssw/listen l :mouse-exited
                (fn [_] (ssw/config! l :background bg)))
    l))

(defn button-label [on-click & options]
  (let [l (apply ssw/label options)]
    (configure-button-label l on-click)))

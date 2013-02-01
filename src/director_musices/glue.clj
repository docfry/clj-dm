(ns director-musices.glue
  (:use clojure.java.io
        (director-musices 
          interpreter
          [utils :only [with-indeterminate-progress]]))
  (:require [seesaw.core :as ssw]))

(def dm-init? (atom nil))

(defn load-package-dm []
  (load-abcl "dm:package-dm.lsp"))

(defn load-core []
  (load-multiple-abcl
    "dm:lib-core:"
    ["scoreobjects.lsp" "basicmacros.lsp" "infixmath.lsp" "musicio.lsp" "rulemacros.lsp" "parallelrulemacros.lsp" 
     "dm-objects.lsp" "initconvert.lsp" "save-pdm-score.lsp" "rule-groups.lsp" "syntobjects.lsp" "shapeobjects.lsp"
     "midifileoutput.lsp" "midifileinput.lsp" "playlist.lsp" "midibasic-lw.lsp"])
  (load-abcl "dm:init.lsp"))

(defn load-rules []
  (load-multiple-abcl
    "dm:rules:"
    ["frules1.lsp" "frules2.lsp" "Intonation.lsp"
     "FinalRitard.lsp" "utilityrules.lsp" "Punctuation.lsp"
     "phrasearch.lsp" "SyncOnMel.lsp"]))

(defn init-dm []
  (when-not @dm-init?
    (with-indeterminate-progress "Loading lisp environment"
      (load-package-dm)
      (load-core)
      (load-rules)
      (swap! dm-init? (constantly true)))))

;(use 'clojure.pprint)

(defn load-active-score [string]
  (init-dm)
  (eval-abcl 
    (str "(in-package :dm)
          (read-active-score-from-string \"" string 
         "\")
          (init-music-score)")))
;  (eval-abcl "(in-package :dm)")
;  (.execute (abcl-f "DM" "read-score-from-string") (str->abcl string))
;  (eval-abcl "(init-music-score)"))

(defn load-active-score-from-file [path]
  (init-dm)
  (eval-abcl "(in-package :dm)")
  (.execute (abcl-f "DM" "read-active-score-from-file") (abcl-path-str path))
  (eval-abcl "(init-music-score)"))

(defn load-active-score-from-midi-file [path]
  (init-dm)
  (eval-abcl "(in-package :dm)")
  (.execute (abcl-f "DM" "load-midifile-fpath") (abcl-path-str path))
  (eval-abcl "(init-music-score)"))

(defn get-active-score []
  (.execute (abcl-f "DM" "get-active-score")))

(defn apply-rules [rulelist-string sync-rule & [rule-interaction-c]]
  (init-dm)
  (if rule-interaction-c
    (eval-abcl (str "(in-package :dm)
                    (reset-music)
                    (rule-interaction-apply-rules-sync '(" rulelist-string ") '" rule-interaction-c ")"))
    (eval-abcl (str "(in-package :dm)
                    (reset-music)
                    (rule-apply-list-sync '(" 
                    rulelist-string ") '" sync-rule ")"))))

(defn save-midi-to-path [path]
  (init-dm)
  (eval-abcl "(in-package :dm)")
  (.execute (abcl-f "DM" "save-performance-midifile1-fpath") (abcl-path-str path)))

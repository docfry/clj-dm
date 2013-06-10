(ns reload
  (:use [clojure.tools.namespace.repl :only [refresh]]))

(defn reload []
  (refresh :after 'director-musices.core/reload-ui))

(defn reload-command-line []
  (refresh :after 'director-musices.common-lisp.command-line/run-test))

(defproject director-musices "1.0.0-SNAPSHOT"
  :description "FIXME: write description"
  :disable-deps-clean true
  :main director-musices.core
;  :aot [director-musices.glue]
;  :extra-classpath-dirs ["abc4j/lib/abc4j.jar"]
;  :library-path ["lib/" "abc4j/lib/"]
  :dependencies [[org.clojure/clojure "1.2.1"]
                 [org.clojure/tools.logging "0.1.2"]
                 [hafni "1.0.5-SNAPSHOT"]
                 [com.miglayout/miglayout "3.7.4"]
                 [org.armedbear.lisp/abcl "0.25.0"]])

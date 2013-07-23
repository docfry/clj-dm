(defproject 
  director-musices "3.0.2"
  :description "FIXME: write description"
  :manifest ["SplashScreen-Image" "splash.gif"]
  :dependencies [[org.clojure/clojure "1.5.0"]
                 [seesaw "1.4.3"]
                 [com.miglayout/miglayout "3.7.4"]
                 [abcl "1.1.1"]
                 [svg-salamander "1.0"]
                 [org.clojure/tools.cli "0.2.2"]
                 [com.taoensso/timbre "1.5.2"]
                 [instaparse "1.2.2"]]
  :profiles {:dev {:dependencies [[org.clojure/tools.namespace "0.2.2"]]}
             :jar {:main director-musices.main}})

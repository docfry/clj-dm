(defproject 
  director-musices "3.1.6.alpha"
  :description "FIXME: write description"
  ;:manifest ["SplashScreen-Image" "splash.gif"] ;did not work for uberjar
  :dependencies [[org.clojure/clojure "1.12.0"] ;[org.clojure/clojure "1.10.1"];[org.clojure/clojure "1.5.0"]
                 [seesaw "1.5.0"] ;[seesaw "1.4.3"] ;not updated since DM 3.1.3
                 [org.abcl/abcl "1.6.1"] ;[abcl "1.3.1"]
                 [com.weblookandfeel/svg-salamander "1.1.2.2"] ;[svg-salamander "1.0"]
                 [org.clojure/tools.cli "1.1.230"];[org.clojure/tools.cli "1.0.194"]; [org.clojure/tools.cli "0.2.2"]
                 [com.taoensso/timbre "3.4.0"] ;last ver that works, generate a lot of text
                 ;[com.taoensso/timbre "4.10.0"] latest but not working
                 ;[com.taoensso/timbre "1.5.2"] original -ok
                 [instaparse "1.4.10"] ;[instaparse "1.2.2"]
                 ]
  :profiles {:dev {:dependencies [[org.clojure/tools.namespace "1.5.0"];[org.clojure/tools.namespace "1.0.0"] ;[org.clojure/tools.namespace "0.2.2"]
                                  ]}
             :jar {:main director-musices.main}
             ;:uberjar {:main director-musices.main}
             }
  ;:main ^:skip-aot director-musices.main ;is not working
  :main director-musices.main
  :aot [director-musices.main]  ;made lein uberjar hang.../af - quit the MAIN during processing!!!!
  )

(defproject cljs-rum "0.1.0-SNAPSHOT"
  :description ""
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}

  :min-lein-version "2.7.1"

  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.clojure/clojurescript "1.10.339"]
                 [org.clojure/core.async "0.4.474"]

                 [cljsjs/react "16.3.2-0"]
                 [cljsjs/react-dom "16.3.2-0"]
                 [cljsjs/prop-types "15.6.1-0"]
                 [rum "0.11.2"]
                 [sablono "0.8.4"]

                 [metosin/reitit-core "0.1.4-SNAPSHOT"]]

  :plugins [[lein-figwheel "0.5.16"]
            [lein-cljsbuild "1.1.7" :exclusions [[org.clojure/clojure]]]]

  :source-paths ["src"]
  :resource-paths ["resources" "target/cljsbuild"]

  :cljsbuild {:builds
              [{:id           "dev"
                :source-paths ["src"]

                ;; The presence of a :figwheel configuration here
                ;; will cause figwheel to inject the figwheel client
                ;; into your build
                :figwheel     {:on-jsload "cljsempty.core/on-js-reload"}

                :compiler     {:main                 cljsempty.core
                               :asset-path           "js/out"
                               :output-dir           "target/cljsbuild/public/js/out"
                               :modules              {:base        {:output-to "target/cljsbuild/public/js/app.js"
                                                                    :entries   #{"cljsempty.core"}}
                                                      :transaction {:output-to  "target/cljsbuild/public/js/transaction.js"
                                                                    :entries    #{"cljsempty.transaction"}
                                                                    :depends-on #{:base}}
                                                      :cljs-base   {:output-to "target/cljsbuild/public/js/core.js"}}
                               :source-map-timestamp true
                               :preloads             [devtools.preload]}}

               {:id           "prod"
                :source-paths ["src"]

                :compiler     {:main          cljsempty.core
                               :asset-path    "js/out"
                               :output-dir    "target/cljsbuild/prod/js/out"
                               :optimizations :advanced
                               :modules       {:base        {:output-to "target/cljsbuild/prod/app.js"
                                                             :entries   #{"cljsempty.core"}}
                                               :transaction {:output-to  "target/cljsbuild/prod/transaction.js"
                                                             :entries    #{"cljsempty.transaction"}
                                                             :depends-on #{:base}}
                                               :cljs-base   {:output-to "target/cljsbuild/prod/core.js"}}}}]}

  :figwheel {:css-dirs    ["resources/public/css"]
             :server-port 3450}

  :profiles {:dev {:dependencies [[binaryage/devtools "0.9.9"]
                                  [figwheel-sidecar "0.5.16"]
                                  [cider/piggieback "0.3.1"]]
                   ;; need to add dev source path here to get user.clj loaded
                   :source-paths ["src" "dev"]
                   ;; for CIDER
                   ;; :plugins [[cider/cider-nrepl "0.12.0"]]
                   :repl-options {:nrepl-middleware [cider.piggieback/wrap-cljs-repl]}}})

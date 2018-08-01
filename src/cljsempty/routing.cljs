(ns cljsempty.routing
  (:require [reitit.core :as reitit]
            [goog.events :as e]
            [goog.History :as h])
  (:import [goog History]))

(defonce current-route (atom {}))
(defonce routes-registry (atom {}))
(defonce router (atom nil))

(defn register-route!
  [id title route view options]
  (let [routes (swap! routes-registry
                      assoc id
                      (merge options
                             {:id    id
                              :title title
                              :route route
                              :view  view}))]

    (reset! router
            (reitit/router
              (reduce
                (fn [a v]
                  (conj a
                        (conj (:route v) (:id v))))
                []
                (vals routes))))

    routes))

(defonce controllers
         (atom {}))

(defn- handle-history-event [handler e]
  (let [new (reitit/match-by-path
              @router
              (.-token e))
        old @current-route
        id (get-in new [:data :name])
        rtinfo (get @routes-registry id)]

    (when-let [initfn (:init rtinfo)]
      (initfn new))

    (reset! current-route new)
    (handler new)))

(defonce history-holder (atom nil))

(defn start-router [handler]
  (let [history (History. false nil (js/document.getElementById "history_state"))]
    (e/listen history h/EventType.NAVIGATE
              (fn [e]
                (handle-history-event handler e)))
    (.setEnabled history true)
    (reset! history-holder history)))

(defn get-route-info [id]
  (get @routes-registry id))

(defn depart [id params]
  (let [match (reitit/match-by-name
                @router
                id
                params)]
    (get match :path)))

(defn go-silent [id params]
  (let [match (reitit/match-by-name
                @router
                id
                params)
        path (get match :path)]
    (.replaceToken @history-holder path)))

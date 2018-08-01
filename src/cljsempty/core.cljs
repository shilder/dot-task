(ns cljsempty.core
  (:require [rum.core :as rum]
            [cljsempty.routing :as router]
            [cljs.loader :as loader]
            [clojure.string :as str]
            [om.dom :as dom]))

(enable-console-print!)

(def data {"1"   {:id          "1",
                  :created_at  #inst"2018-07-17T06:37:00.656Z",
                  :title       "News item 1",
                  :description "This is very interesting news This is very interesting news This is very interesting news This is very interesting news This is very interesting news This is very interesting news",
                  :has_read    false
                  :type        "news"}
           "131" {:description "Some money flown",
                  :amount      "10",
                  :type        "transaction",
                  :title       "Transaction 1",
                  :currency    "RUR",
                  :id          "131",
                  :sender      "Sender data",
                  :receiver    "Receiver"
                  :direction   "in",
                  :created_at  #inst"2018-07-17T06:37:22"}
           "3"   {:description "Some money flown This is very interesting news This is very interesting news",
                  :amount      "10",
                  :type        "transaction",
                  :title       "Transaction 1",
                  :currency    "RUR",
                  :id          "3",
                  :sender      "Sender data",
                  :receiver    "Receiver"
                  :direction   "out",
                  :created_at  #inst"2018-07-17T06:37:10.656Z"}
           "5"   {:id          "5",
                  :created_at  #inst"2018-07-17T01:37:01Z",
                  :title       "News item 2",
                  :description "This is very interesting news",
                  :has_read    false
                  :type        "news"}})

(defonce app-state (atom {:show-transactions? true
                          :show-news?         true
                          :sort-by            :desc}))

(defmulti render-item :type)

(rum/defc unknown-item
  [item]
  [:div.timeline-list-item "Unknown item: " (:type item)])

(defmethod render-item :default
  [item]
  (unknown-item item))

(defn generate-news-item []
  {:id          (str (random-uuid)),
   :created_at  (js/Date.),
   :title       "News item 2",
   :description "This is very interesting news",
   :has_read    false
   :type        "news"})

(defn generate-transaction-item []
  {:description "Some money flown This is very interesting news This is very interesting news",
   :amount      (rand-int 1000),
   :type        "transaction",
   :title       (str "Transaction " (rand-int 20)),
   :currency    "RUR",
   :id          (str (random-uuid)),
   :sender      "Sender",
   :receiver    "Receiver"
   :direction   (if (< (rand-int 10)
                       5)
                  "in"
                  "out"),
   :created_at  (js/Date.)})

(defn add-new-random! []
  (let [i (if (< (rand-int 10)
                 5)
            (generate-news-item)
            (generate-transaction-item))]
    (swap! app-state
           assoc-in [:data (:id i)]
           i)))

(rum/defc checkbox
  [id label value on-change]
  [:div.checkbox
   [:input {:type      "checkbox"
            :checked   value
            :id        id
            :on-change (fn [e]
                         (when-some [v (some-> e
                                               (.-target)
                                               (.-checked))]
                           (on-change v)))}]
   [:label {:for id} label]])

(rum/defc hello-world < rum/reactive
  []
  (let [s (rum/react app-state)
        sort (:sort-by s :asc)]
    [:div.container
     [:div.timeline
      [:h1 "Timeline"]
      [:div.timeline-controls
       [:div.date
        [:a
         {:href     "#"
          :on-click (fn [e]
                      (.preventDefault e)
                      (swap! app-state update :sort-by
                             #(if (= % :asc)
                                :desc
                                :asc)))}
         (str "Время события " (case sort
                                 :desc "↑"
                                 :asc "↓"))]]

       ;; Да, можно было заморочиться с регистрацией нового типа события
       ;; и отрисовкой этой панельки в более автоматическом режиме, но я решил
       ;; что в любом случае надо проверять вёрстку и нет большого смысла в этом
       ;; автоматизме. Если будет много элементов, то придётся делать сложную вёрстку,
       ;; если будет мало элементов, то тогда и генератор не нужен
       [:div.filter
        (checkbox :news
                  "Новости"
                  (:show-news? s)
                  #(swap! app-state assoc :show-news? %))]
       [:div.filter
        (checkbox :transactions
                  "Транзакции"
                  (:show-transactions? s)
                  #(swap! app-state assoc :show-transactions? %))]]
      [:div.timeline-list
       (let [filtered (filter #(case (:type %)
                                 "news"
                                 (:show-news? s)
                                 "transaction"
                                 (:show-transactions? s))
                              (vals (:data s)))
             sorted (sort-by :created_at filtered)
             sorted (if (= :desc sort)
                      (reverse sorted)
                      sorted)]
         (for [i sorted]
           (rum/with-key
             (render-item i)
             (:id i))))]]

     [:div.button
      [:button.btn-primary
       {:on-click #(add-new-random!)}
       "Добавить"]]]))

(rum/defc link < rum/static
  [title id params]
  [:a
   {:href (str "#" (router/depart id params))}
   title])

(defn format-datetime
  [ms]
  (when ms
    (.toLocaleString (js/Date. ms))))

(rum/defc datetime < rum/static
  [date]
  [:div.date
   (format-datetime date)])

(defn classnames
  [& parts]
  (str/join " "
            (reduce
              (fn [a [c v]]
                (if c
                  (conj a v)
                  a))
              []
              (partition 2 parts))))

(rum/defcs news-item
  < (rum/local {})
    rum/static
  [rumstate item]
  (let [s (:rum/local rumstate)]
    [:div.timeline-list-item.news-item
     {:class (classnames
               (:collapsed? @s) "collapsed"
               (:has_read item) "read")}

     (datetime (:created_at item))
     (link (:title item) ::news {:id (:id item)})

     (when (:has_read item)
       [:div.right
        [:span.hide
         {:on-click (fn [e]
                      (swap! s assoc :collapsed? true)
                      (js/setTimeout
                        #(swap! app-state update :data dissoc (:id item))
                        200))}
         "Удалить"]])]))

(defmethod render-item "news"
  [item]
  (news-item item))

(rum/defc info < rum/static
  [title value]
  [:div.info-row
   [:div.title title]
   [:div.content value]])

(rum/defc news-info
  [item]
  [:div.container
   [:h2 "Новость " (:id item)]
   (info "Дата публикации"
         (format-datetime (:created_at item)))
   (info "Заголовок"
         (:title item))

   [:div.news-body
    (:description item)]

   (when-not (:has_read item)
     [:div.centered
      [:button.btn-primary
       {:on-click (fn [e]
                    (swap! app-state
                           update-in [:data (:id item)]
                           assoc :has_read true)
                    (router/go-silent ::timeline {}))}
       "Ознакомлен"]])])

(router/register-route!
  ::timeline
  "Просмотр ленты"
  ["/timeline"]
  (fn [route]
    (hello-world))
  {:init (fn [route]
           ; Load data if needed
           (when-not (:data @app-state)
             (swap! app-state assoc :data data)))})

(router/register-route!
  ::news
  "Просмотр подробностей новости"
  ["/timeline/news/:id"]
  (fn [route]
    (let [id (get-in route [:path-params :id])
          item (get (:data @app-state) id)]
      (news-info item)))
  {})

(router/register-route!
  ::index
  "Начальный маршрут"
  [""]
  (fn [route])
  {:init (fn [route]
           ; Переходим на таймлайн
           (js/setTimeout
             #(router/go-silent ::timeline {})
             100))})

(defn mount-route [route]
  (let [id (get-in route [:data :name])
        rtinfo (router/get-route-info id)
        view (:view rtinfo)]
    (rum/mount (view route)
               (js/document.getElementById "app"))))

(defn on-js-reload []
  (mount-route @router/current-route))

(defn load-module [id]
  (loader/load id
               (fn []
                 ; reload after new module is installed
                 (on-js-reload))))

(defonce history (router/start-router mount-route))

(loader/set-loaded! :base)

(js/setTimeout
  (fn []
    (load-module :transaction))
  3000)

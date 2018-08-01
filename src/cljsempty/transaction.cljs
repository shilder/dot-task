(ns cljsempty.transaction
  (:require [cljs.loader :as loader]
            [cljsempty.core :as c]
            [rum.core :as rum]
            [sablono.core :refer [html]]
            [cljsempty.routing :as router]))

(rum/defc transaction-item
  [item]
  [:div.timeline-list-item.transaction-item
   (c/datetime (:created_at item))

   (c/link
     (case (:direction item)
       "in"
       (html [:span [:span.in (str "+" (:amount item) " " (:currency item))] " от " (:sender item)])
       "out"
       (html [:span [:span.out (str "-" (:amount item) " " (:currency item))] " к " (:receiver item)]))
     ::transaction
     {:id (:id item)})])

(defmethod c/render-item "transaction"
  [item]
  (transaction-item item))

(rum/defc transaction-info
  [item]
  [:div.container
   [:h2 "Данные по транзакции " (:id item)]
   (c/info "Дата операции"
           (c/format-datetime (:created_at item)))
   (c/info "Тип операции"
           (case (:direction item)
             "in"
             "Приход"
             "out"
             "Расход"))
   (c/info "Отправитель"
           (:sender item))
   (c/info "Получатель"
           (:receiver item))
   (c/info "Сумма"
           (str (:amount item) " " (:currency item)))
   (c/info "Наименование"
           (:title item))
   (c/info "Описание"
           (:description item))

   [:div.centered
    [:button.btn-primary
     {:on-click (fn [e]
                  (swap! c/app-state
                         update :data
                         dissoc (:id item))
                  (router/go-silent ::c/timeline {}))}
     "Удалить"]]])

(router/register-route!
  ::transaction
  "Просмотр подробностей транзакции"
  ["/timeline/transaction/:id"]
  (fn [route]
    (let [id (get-in route [:path-params :id])
          item (get (:data @c/app-state) id)]
      (transaction-info item)))
  {})

(loader/set-loaded! :transaction)

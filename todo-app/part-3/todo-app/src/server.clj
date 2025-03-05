(ns server
  (:require
   [datomic.api :as d]
   [hiccup.page :as hp]
   [hiccup2.core :as h]
   [io.pedestal.http :as http]
   [io.pedestal.http.route :as route]
   [todo-db-2 :as todo-db]))

(defn gen-page-head
  "Include bootstrap css and js"
  [title]
  [:head
   [:title title]
   (hp/include-css "https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css")
   (hp/include-js "https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js")])

(defn all-lists-page
  "Renders the TODO list page"
  [_request]
  (str
   (h/html
    (gen-page-head "Datolister")
    [:div {:class "container" :style "padding-top: 50px"}
     [:h1 "Lists"]
     [:div {:class "row row-cols-2"}
      (for [list (todo-db/lists-page (d/db todo-db/conn)) ;; Important line
            :let [list-name (:list/name list)]]
        [:div {:class "col card"}
         [:div {:class "card-body"}
          [:h4 {:class "card-title"} list-name]
          [:table {:class "table mb-4"}
           [:thead
            [:tr
             [:th {:scope "col"} "Item"]
             [:th {:scope "col"} "Status"]
             [:th {:scope "col"} "Actions"]]]
           [:tbody
            (for [item (:list/items list)
                  :let [item-text (get item :item/text)
                        item-status (get-in item [:item/status :db/ident])]]
              [:tr
               [:td item-text]
               [:td
                [:span {:class "badge text-bg-light"} item-status]]
               [:td
                [:div {:class "row row-cols-auto row-cols-sm"}]]])]]]])]])))

(defn html-200 [body]
  {:status  200
   :headers {"Content-Type" "text/html; charset=utf-8"}
   :body    body})

(def routes
  (route/expand-routes
   #{["/" :get (comp html-200 all-lists-page) :route-name :home]}))

(defn create-server []
  (http/create-server
   {::http/routes routes
    ::http/secure-headers {:content-security-policy-settings "object-src 'none'; script-src 'self' 'unsafe-inline' 'unsafe-eval' https: http:;"}
    ::http/type :jetty
    ::http/port 8890
    ::http/join? false}))

(defonce server (atom nil))

(defn start-server []
  (reset! server (-> (create-server) http/start)))

(defn stop-server []
  (swap! server http/stop))

(defn restart-server []
  (stop-server)
  (start-server))
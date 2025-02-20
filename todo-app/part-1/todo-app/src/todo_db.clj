(ns todo-db
  (:require
   [clojure.string :as string]
   [datomic.api :as d]))

(def schema
  [{:db/ident       :list/name
    :db/valueType   :db.type/string
    :db/cardinality :db.cardinality/one
    :db/unique      :db.unique/identity
    :db/doc         "List name"}
   {:db/ident       :list/items
    :db/valueType   :db.type/ref
    :db/cardinality :db.cardinality/many
    :db/doc         "List items reference"}
   {:db/ident       :item/status
    :db/valueType   :db.type/ref
    :db/cardinality :db.cardinality/one
    :db/doc         "Item Status"}
   {:db/ident       :item/text
    :db/valueType   :db.type/string
    :db/cardinality :db.cardinality/one
    :db/doc         "Item text"}
   {:db/ident :item.status/todo}
   {:db/ident :item.status/doing}
   {:db/ident :item.status/done}])

(def db-uri "datomic:dev://localhost:4334/todo")

;; INFO: Creates the database, if it does not exist returns false
(d/create-database db-uri) ;; it requires a running transactor

;; INFO: to delete a database use `d/delete-database`
(comment (d/delete-database db-uri))

;; INFO: Establish connection to the database
(def conn (d/connect db-uri))

(comment @(d/transact conn schema))

(defn ensure-schema
  "verify that schema is transacted"
  [conn]
  (or (-> conn d/db (d/entid :list/name))
      @(d/transact conn schema)))

(defn new-list [list-name]
  [:db/add "list.id" :list/name list-name])

(defn new-item [db list-name item-text]
  (let [minify (string/replace item-text #" " "-")]
    {:db/id (d/entid db [:list/name list-name])
     :list/items [{:db/id  (str "item.temp." minify)
                   :item/text item-text
                   :item/status :item.status/todo}]}))
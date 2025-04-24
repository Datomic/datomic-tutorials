# Building a TODO List App with Clojure + Datomic Pro - [Part 4]

In [Part 3](../../part-3/todo-app/README.md) we completed the following:

- Create and Delete Lists
- Create, Update and Delete Items
- Provide UI via HTML forms using Hiccup and receiving requet to the HTTP Pedestal server

We are ready to the best section where we will showcase one of the main powers of Datomic..... time and history. Part 4 is about leveraging accumulate only property. We will implement time-basis filters, [as-of](https://docs.datomic.com/reference/filters.html#as-of) and [since](https://docs.datomic.com/reference/filters.html#since), that will allow us to render the state of our TODO app at different moments in time. Provide a history of status changes, when and to what the item transitioned. That will allow us to render the state of our TODO app at different moments in time and to finish, a filter by status.

- Add time-basis filters (as-of and since)
- Display history of status transition
- Add status filter

### Building the app


#### Time Filters, As-of and Since

To start with, run the REPL in your editor or in the terminal.

```shell
clj
```

```shell
;; REPL
user>
```

#### REPL Explorations

Before diving into the code, we are going to do a quick exploration on the [d/as-of](https://docs.datomic.com/clojure/index.html#datomic.api/as-of) and [d/since]([d/as-of](https://docs.datomic.com/clojure/index.html#datomic.api/since) APIs.

In part 1 we glanced quickly into making a query to the past by accessing `:db-before` in the `tx-report`

```clojure
(d/q '[:find ?list
       :in $
       :where [?list :list/name "learn"]]
      (:db-before tx-report-learn)) ;; IMPORTANT line
```
The idea is the same, pass a db as a value where the db points at "some" moment of time. The difference is that now we want to explicitly tell to which moment, not only the `:db-before`, it can be yesterday, the day before or any date time. Let's see an example:

```clojure
(d/as-of (d/db conn) #inst "2025-01-01")
```
This will give us a `db` including data until `2025-01-01`, data added after that time will not be considered in the query. Let's do an example with our current TODO app

```clojure
;; REPL
(lists-page (d/as-of (d/db conn) #inst "2025-02-27"))
```

```clojure
;; Result
[{:db/id 17592186045421, 
  :list/name "life", :list/items [{:db/id 17592186045425, :item/text "travel", 
                                   :item/status #:db{:ident :item.status/waiting}} 
                                  {:db/id 17592186045427, 
                                   :item/text "play drums", 
                                   :item/status #:db{:ident :item.status/waiting}} 
                                  {:db/id 17592186045428, 
                                   :item/text "scuba dive", 
                                   :item/status #:db{:ident :item.status/waiting}} 
                                  {:db/id 17592186045429, 
                                   :item/text "buy coffee", 
                                   :item/status #:db{:ident :item.status/waiting}}]} 
 {:db/id 17592186045423, :list/name "learn"}]
```

In this example I'm using #inst "2025-02-27" which is the time when the tutorial was been made, in your case try passing a inst based on the current time. The result here returns the list "life" with some items and list "learn" without any items, at that moment of time the "learn" list didn't have items yet.

#### Retract Item

### Resources

- [Pedestal routes](http://pedestal.io/pedestal/0.7/guides/defining-routes.html)
- [Hiccup basic syntax](https://github.com/weavejester/hiccup/wiki/Syntax)
- [Datomic - :db/ident](https://docs.datomic.com/schema/identity.html#idents)
- [Datomic - pull](https://docs.datomic.com/query/query-pull.html)
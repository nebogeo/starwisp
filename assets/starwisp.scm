(define (layout width height weight gravity) (list "layout" width height weight gravity))
(define (layout-width l) (list-ref l 1))
(define (layout-height l) (list-ref l 2))
(define (layout-weight l) (list-ref l 3))
(define (layout-gravity l) (list-ref l 4))

(define (linear-layout id orientation layout children)
  (list "linear-layout" id orientation layout children))
(define (linear-layout-id t) (list-ref t 1))
(define (linear-layout-orientation t) (list-ref t 2))
(define (linear-layout-layout t) (list-ref t 3))
(define (linear-layout-children t) (list-ref t 4))

(define (text-view id text size layout) (list "text-view" id text size layout))
(define (text-view-id t) (list-ref t 1))
(define (text-view-text t) (list-ref t 2))
(define (text-view-modify-text t v) (list-replace t 2 v))
(define (text-view-size t) (list-ref t 3))
(define (text-view-layout t) (list-ref t 4))

(define (edit-text id text size layout listener) (list "edit-text" id text size layout listener))
(define (edit-text-id t) (list-ref t 1))
(define (edit-text-text t) (list-ref t 2))
(define (edit-text-modify-text t v) (list-replace t 2 v))
(define (edit-text-size t) (list-ref t 3))
(define (edit-text-layout t) (list-ref t 4))
(define (edit-text-listener t) (list-ref t 5))

(define (button id text text-size layout listener) (list "button" id text text-size layout listener))
(define (button-id t) (list-ref t 1))
(define (button-text t) (list-ref t 2))
(define (button-modify-text t v) (list-replace t 2 v))
(define (button-text-size t) (list-ref t 3))
(define (button-layout t) (list-ref t 4))
(define (button-listener t) (list-ref t 5))

(define (seek-bar id max layout listener) (list "seek-bar" id max layout listener))
(define (seek-bar-id t) (list-ref t 1))
(define (seek-bar-max t) (list-ref t 2))
(define (seek-bar-layout t) (list-ref t 3))
(define (button-listener t) (list-ref t 4))

(define (spinner id items layout listener) (list "spinner" id items layout listener))
(define (spinner-id t) (list-ref t 1))
(define (spinner-items t) (list-ref t 2))
(define (spinner-layout t) (list-ref t 3))
(define (spinner-listener t) (list-ref t 4))

(define (toast msg) (list "toast" 0 "toast" msg))

(define (update-widget type id token value) (list type id token value))

(define id-map ())

(define (find-id name id-map)
  (cond
   ((null? id-map) #f)
   ((equal? name (car (car id-map))) (cadr (car id-map)))
   (else (find-id name (cdr id-map)))))

(define (get-id name)
  (find-id name id-map))

(define (make-id name)
  (set! id-map (cons (list name (length id-map)) id-map))
  (get-id name))

(define wrap (layout 'wrap-content 'wrap-content 1 'left))
(define fillwrap (layout 'fill-parent 'wrap-content 1 'left))
(define wrapfill (layout 'wrap-content 'fill-parent 1 'left))
(define fill (layout 'fill-parent 'fill-parent 1 'left))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


(define (clicker-clicked)
  (send
   (scheme->json
    (list
     (toast "hello dudes")
     (update-widget 'text-view (get-id "view1") 'text "I have been updated")))))

(define (seekin v)
  (send
   (scheme->json
    (list (update-widget 'text-view (get-id "view2") 'text (number->string v))))))

(define (spin v)
  (send
   (scheme->json
    (list (update-widget 'text-view (get-id "view3") 'text v)))))

(define (hider)
  (send
   (scheme->json
    (list
     (update-widget 'text-view 999 'hide 0)))))


;01326619546
;r6n5n7x6a3

(define (editlisten v)
  (send
   (scheme->json
    (list (update-widget 'text-view 999 'text v)))))

(send (scheme->json
       (linear-layout
        (make-id "top")
        'vertical
        (layout 'fill-parent 'fill-parent 1 'left)
        (list
         (spinner (make-id "spinner") (list "one" "two" "three" "cows") fillwrap 'spin)
         (edit-text (make-id "name") "Name" 20 fillwrap 'editlisten)
         (linear-layout
          (make-id "foo")
          'horizontal
          (layout 'fill-parent 'fill-parent 1 'centre)
          (list
           (button (make-id "but1") "Click me" 20 (layout 'wrap-content 'wrap-content 0 'centre) 'hider)
           (button (make-id "but3") "Boo" 20 (layout 'wrap-content 'wrap-content 0 'centre) 'hider)))
         (text-view (make-id "view1") "This is the title" 10 fillwrap)
         (text-view (make-id "view2") "More texht" 40 fillwrap)
         (text-view (make-id "view3") "event More texht" 30 fillwrap)
         (button (make-id "but2") "Click me also pretty please" 20 fillwrap 'clicker-clicked)
         (seek-bar (make-id "seek") 100 fillwrap 'seekin)
         ))))

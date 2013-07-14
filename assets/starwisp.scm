;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(define (activity name on-create on-start on-resume on-pause on-stop on-destroy on-activity-result)
  (list on-create on-start on-resume on-pause on-stop on-destroy on-activity-result))

(define (activity-name a) (list-ref a 0))
(define (activity-on-create a) (list-ref a 1))
(define (activity-on-start a) (list-ref a 2))
(define (activity-on-resume a) (list-ref a 3))
(define (activity-on-pause a) (list-ref a 4))
(define (activity-on-stop a) (list-ref a 5))
(define (activity-on-destroy a) (list-ref a 6))
(define (activity-on-activity-result a) (list-ref a 7))

(define (activity-list l)
  (list l))

(define (activity-list-find l name)
  (cond
   ((null? l) #f)
   ((eq? (activity-name (car l)) name) (car l))
   (else (activity-list-find (cdr l) name))))


;; called by java
(define (callback name type callback-type)
  0 ;; search structure for the right callback
  )

(define root 0)

(define (define-activity-list . args)
  (set! root (activity-list args)))


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

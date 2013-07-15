;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


(define-activity-list
  (activity
   "main"
   (linear-layout
    (make-id "top")
    'vertical
    (layout 'fill-parent 'fill-parent 1 'left)
    (list
     (spinner (make-id "spinner") (list "one" "two" "three" "cows") fillwrap
              (lambda (v)
                (display "spinner fn called")(newline)
                (list (update-widget 'text-view (get-id "view3") 'text v))))
     (edit-text (make-id "name") "Name" 20 fillwrap
                (lambda (v) (list (update-widget 'text-view 999 'text v))))
     (linear-layout
      (make-id "foo")
      'horizontal
      (layout 'fill-parent 'fill-parent 1 'centre)
      (list
       (button (make-id "but1") "Click me" 20 (layout 'wrap-content 'wrap-content 0 'centre)
               (lambda () (list (update-widget 'text-view 999 'hide 0))))
       (button (make-id "but3") "Boo" 20 (layout 'wrap-content 'wrap-content 0 'centre)
               (lambda () (list (update-widget 'text-view 999 'hide 0))))))

     (text-view (make-id "view1") "This is the title" 10 fillwrap)
     (text-view (make-id "view2") "More texht" 40 fillwrap)
     (text-view (make-id "view3") "event More texht" 30 fillwrap)

     (button (make-id "but2") "Click me also pretty please" 20 fillwrap
             (lambda ()
               (list
                (toast "hello dudes")
                (switch-activity "two" 2)
                (update-widget 'text-view (get-id "view1") 'text "I have been updated"))))
     (seek-bar (make-id "seek") 100 fillwrap
               (lambda (v)
                 (list (update-widget 'text-view (get-id "view2") 'text (number->string v)))))
     ))

   (lambda (activity)
     (activity-layout activity))
   (lambda (activity) '())
   (lambda (activity) '())
   (lambda (activity) '())
   (lambda (activity) '())
   (lambda (activity) '())
   (lambda (activity) '()))

  (activity
   "two"
   (linear-layout)
    (make-id "top")
    'vertical
    (layout 'fill-parent 'fill-parent 1 'left)
    (list
     (spinner (make-id "spinner") (list "one" "two" "three" "cows") fillwrap
              (lambda (v)

                (list (toast "what's up doc?")))))))

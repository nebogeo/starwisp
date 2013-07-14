; utils funcs for using lists as sets
(define (set-remove a l)
  (if (null? l)
      '()
      (if (eq? (car l) a)
          (set-remove a (cdr l))
          (cons (car l) (set-remove a (cdr l))))))

(define (set-add a l)
  (if (not (memq a l))
      (cons a l)
      l))

(define (set-contains a l)
  (if (not (memq a l))
      #f
      #t))

;; missing list stuff

(define (build-list fn n)
  (define (_ fn n l)
    (cond ((zero? n) l)
          (else
           (_ fn (- n 1) (cons (fn (- n 1)) l)))))
  (_ fn n '()))

(define (foldl op initial seq)
  (define (iter result rest)
    (if (null? rest)
        result
        (iter (op (car rest) result) (cdr rest))))
  (iter initial seq))

(define (insert-to i p l)
  (cond
   ((null? l) (list i))
   ((zero? p) (cons i l))
   (else
    (cons (car l) (insert-to i (- p 1) (cdr l))))))

;; (list-replace '(1 2 3 4) 2 100) => '(1 2 100 4)
(define (list-replace l i v)
  (cond
    ((null? l) l)
    ((zero? i) (cons v (list-replace (cdr l) (- i 1) v)))
    (else (cons (car l) (list-replace (cdr l) (- i 1) v)))))

;; random

(define random-maker
  (let* ((multiplier 48271)
         (modulus 2147483647)
         (apply-congruence
          (lambda (current-seed)
            (let ((candidate (modulo (* current-seed multiplier)
                                     modulus)))
              (if (zero? candidate)
                  modulus
                  candidate))))
         (coerce
          (lambda (proposed-seed)
            (if (integer? proposed-seed)
                (- modulus (modulo proposed-seed modulus))
                19860617))))  ;; an arbitrarily chosen birthday
  (lambda (initial-seed)
    (let ((seed (coerce initial-seed)))
      (lambda args
        (cond ((null? args)
               (set! seed (apply-congruence seed))
               (/ (- modulus seed) modulus))
              ((null? (cdr args))
               (let* ((proposed-top
                       (ceiling (abs (car args))))
                      (exact-top
                       (if (inexact? proposed-top)
                           (inexact->exact proposed-top)
                           proposed-top))
                      (top
                       (if (zero? exact-top)
                           1
                           exact-top)))
                 (set! seed (apply-congruence seed))
                 (inexact->exact (floor (* top (/ seed modulus))))))
              ((eq? (cadr args) 'reset)
               (set! seed (coerce (car args))))
              (else
               (display "random: unrecognized message")
               (newline))))))))

(define random
  (random-maker 19781116))  ;; another arbitrarily chosen birthday

(define rndf random)

(define (rndvec) (vector (rndf) (rndf) (rndf)))

(define (crndf)
  (* (- (rndf) 0.5) 2))

(define (crndvec)
  (vector (crndf) (crndf) (crndf)))

(define (srndvec)
  (let loop ((v (crndvec)))
    (if (> (vmag v) 1) ; todo: use non sqrt version
        (loop (crndvec))
        v)))

(define (hsrndvec)
  (let loop ((v (crndvec)))
    (let ((l (vmag v)))
      (if (or (> l 1) (eq? l 0))
          (loop (crndvec))
          (vdiv v l)))))

(define (grndf)
  (let loop ((x (crndf)) (y (crndf)))
    (let ((l (+ (* x x) (* y y))))
      (if (or (>= l 1) (eq? l 0))
          (loop (crndf) (crndf))
          (* (sqrt (/ (* -2 (log l)) l)) x)))))

(define (grndvec)
  (vector (grndf) (grndf) (grndf)))

(define (rndbary)
	(let*
		((a (- 1.0 (sqrt (rndf))))
		 (b (* (rndf) (- 1.0 a)))
		 (c (- 1.0 (+ a b))))
		(vector a b c)))

; return a line on the hemisphere
(define (rndhemi n)
  (let loop ((v (srndvec)))
    (if (> (vdot n v) 0)
        v
        (loop (srndvec)))))

(define (hrndhemi n)
  (let loop ((v (hsrndvec)))
    (if (> (vdot n v) 0)
        v
        (loop (hsrndvec)))))
                                        ;
;; convert scheme values into equivilent json strings

(define (scheme->json v)
  (cond
   ((number? v) (number->string v))
   ((symbol? v) (string-append "\"" (symbol->string v) "\""))
   ((string? v) (string-append "\"" v "\""))
   ((boolean? v) (if v "true" "false"))
   ((list? v)
    (cond
     ((null? v) "null")
     (else
      ; if it quacks like an assoc list...
      (if (and (not (null? v)) (not (list? (car v))) (pair? (car v)))
          (assoc->json v)
          (list->json v)))))
   (else (display "value->js, unsupported type for ") (display v) (newline) 0)))

(define (list->json l)
  (define (_ l s)
    (cond
     ((null? l) s)
     (else
      (_ (cdr l)
         (string-append s
                        (if (not (string=? s "")) ", " "")
                        (scheme->json (car l)))))))
  (string-append "[" (_ l "") "]"))

; ((one . 1) (two . "three")) -> { "one": 1, "two": "three }

(define (assoc->json l)
  (define (_ l s)
    (cond
     ((null? l) s)
     (else
      (let ((token (scheme->json (car (car l))))
            (value (scheme->json (cdr (car l)))))
        (_ (cdr l) (string-append s (if (not (string=? s "")) "," "")
                                  "\n" token ": " value))))))
  (string-append "{" (_ l "") "\n" "}"))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

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

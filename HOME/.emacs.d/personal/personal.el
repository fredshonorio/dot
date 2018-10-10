;;; packages
(prelude-require-packages
 '(restclient monokai-theme terraform-mode unicode-fonts))

;;; theme
(load-theme 'monokai t)

;;; fonts

(setq use-default-font-for-symbols nil)

(defvar user/standard-fontset
  (create-fontset-from-fontset-spec standard-fontset-spec)
  "Standard fontset")

(set-fontset-font user/standard-fontset 'unicode
                  (font-spec :family "Symbola")
                  nil 'prepend)

(set-fontset-font user/standard-fontset 'latin
                  (font-spec :family "Iosevka Medium" :size 18)
                  nil 'prepend)

(add-to-list 'default-frame-alist (cons 'font user/standard-fontset))
(add-to-list 'initial-frame-alist (cons 'font user/standard-fontset))

;;; org-mode
;; don't use different font in headers
(custom-set-variables
 '(org-level-color-stars-only t))

;; add time below a TODO when it's marked as DONE
(setq org-log-done 'time)

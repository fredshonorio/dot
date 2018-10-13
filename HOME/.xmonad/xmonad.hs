import XMonad                      (XConfig(..), X(..), Window, WindowSpace
                                   , spawn, xmonad, composeAll, doFloat, stringProperty, doIgnore, className
                                   , (.|.), (<+>), (|||), (-->), (=?)
                                   , shiftMask, modMask, mod4Mask, noModMask
                                   , xK_p, xK_c, xK_q, xK_b, xK_s, xK_f, xK_Print, xK_t
                                   )
import XMonad.Actions.SpawnOn      (spawnHere)
import XMonad.Config.Desktop       (desktopConfig)
import XMonad.Hooks.SetWMName      (setWMName)
import XMonad.Hooks.FadeInactive   (fadeInactiveLogHook)
import XMonad.Hooks.DynamicLog     (xmobarPP, xmobarColor, dynamicLogWithPP, PP(..), shorten)
import XMonad.Hooks.ManageDocks    (avoidStruts)
import XMonad.Hooks.ManageHelpers  (doFullFloat, isFullscreen)
import XMonad.Hooks.Place          (fixed, placeHook)
import XMonad.Layout               (Full(..), Tall(..))
import XMonad.Layout.Grid          (Grid(..))
import XMonad.Layout.ComboP        (SwapWindow(..), Property(..), combineTwoP)
import XMonad.Layout.TwoPane       (TwoPane(..))
import XMonad.Layout.Named         (named)
import XMonad.Layout.NoBorders     (smartBorders)
import XMonad.Layout.Fullscreen    (fullscreenSupport, fullscreenManageHook, fullscreenEventHook, fullscreenFull)
import XMonad.Layout.ToggleLayouts (ToggleLayout(..), toggleLayouts)
import XMonad.Operations           (sendMessage)
import XMonad.Util.Run             (spawnPipe, hPutStrLn, safeSpawn)
import XMonad.Actions.WindowBringer (WindowBringerConfig(..), gotoMenuConfig)
import XMonad.StackSet (tag)
import XMonad.Util.NamedWindows (getName)

import qualified Data.Map as M

-- monokai theme colors
mForeground = "#F8F8F2" 
mBackground = "#272822"
mGreen      = "#A6E22E"
mRed        = "#F92672"

main = do
  xmobarProcess <- spawnPipe "xmobar ~/.xmobarrc"
  xmonad $ desktopConfig
    { terminal           = "sakura"
    , modMask            = mod4Mask
    , keys               = myKeys <+> keys desktopConfig
    , borderWidth        = 5
    , normalBorderColor  = mBackground
    , focusedBorderColor = mForeground
    , startupHook        = startup
    , logHook            = xmobarHook xmobarProcess <+> transparencyHook
    , layoutHook         = avoidStruts . smartBorders $ layouts
    , manageHook         = manageHooks
    }

manageHooks = composeAll
  [ isFullscreen                --> doFullFloat -- without this vlc doesn't correctly come out of full screen
  , wmName    =? "sakura_float" --> placeHook (fixed (0.5, 0.5)) <+> doFullFloat
  , className =? "trayer"       --> doIgnore
  ]
  where
    wmName = stringProperty "WM_NAME"

layouts = toggleLayouts full (tall ||| twoPane ||| grid ||| streams)
  where
    full    = Full              -- fullscreen
    tall    = Tall 1 0.03 0.5   -- tall
    twoPane = TwoPane 0.03 0.5  -- keep only two windows visible
    grid    = Grid              -- a fair-ish grid, usefull for multiple terminals
    streams = named "Streams" $ -- keep non-master windows visible, mod+shift+s swaps windows
      combineTwoP twoPane full grid (Const True)

rofiCfg = WindowBringerConfig "rofi" ["-dmenu", "-i"] decorateName

myKeys (XConfig {modMask = mod}) = M.fromList $
    [ ((mod,               xK_p), spawn "rofi -show run -modi run")
    , ((mod .|. shiftMask, xK_p), spawn "xfce4-appfinder")
    , ((mod,               xK_c), gotoMenuConfig rofiCfg)
    , ((mod .|. shiftMask, xK_q), spawn "xfce4-session-logout")
    , ((mod .|. shiftMask, xK_t), spawn "sakura -t sakura_float -r 20 -c 150")
    , ((mod .|. shiftMask, xK_s), sendMessage $ SwapWindow)   -- only usable in combineTwoP (streams) layout
    , ((mod              , xK_f), sendMessage $ ToggleLayout) -- toggle fullscreen
    , ((noModMask        , xK_Print), spawn "xfce4-screenshooter --fullscreen")
    ]

wallpapers = "$HOME/Tresors/wallpapers/*"

startup = setWMName "LG3D"                                          -- required for java apps
          >> spawnHere ("feh --randomize --bg-fill " ++ wallpapers) -- load random wallpaper

-- transparency for inactive windows
transparencyHook = fadeInactiveLogHook 0.95 -- percent

xmobarHook xmobarProc = dynamicLogWithPP $                 -- setup xmonad to output status to xmobar
  xmobarPP { ppOutput = hPutStrLn xmobarProc               -- write message to xmobar stdin
           , ppTitle = xmobarColor mGreen "" . shorten 140 -- display window title
           , ppCurrent = xmobarColor mRed ""               -- display current workspace
           , ppUrgent = xmobarColor mRed ""
           , ppSep = " ~ "
           }
-- util
decorateName workspace window = do
  name <- show <$> getName window
  return $ "[" ++ tag workspace ++ "] " ++ name

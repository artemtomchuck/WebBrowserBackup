
purpose: before using Firefox as platform for backup you have to setup it properly.

This section will show you how to setup Firefox for allowing it to automatically substitute saved in your browser passwords.
You should use Firefox profile with saved passwords. In most cases you will not need to provide any passwords because sites use cookies for restroing your authentication info.
But some sites may require to provide your password (for example, Google Takeout will require you to provide password when you save your backup to external cloud storage - e.g. to Dropbox).
Saving passwords in code is not an option. Saving passwords in files are also not good option. So let's use standards browser mechanism to save passwords.
The problem with autosubstituion of passwords is that it does not work by default when Firefox is in Marionette mode (controlled by not real user, but some automated code).
To overcome this we have to setup Firefox to redefine the policies.

Firstly find the mozilla.cfg file and copy (or merge the content) into "C:\program files\mozilla firefox\mozilla.cfg"
mozilla.cfg has few settings which effectively say Mozilla to dissallow turning off password autosubstituion so when Marionette runs the browser it will not be able to turn off autopasswords substitution (it turns it off by default which we want to avoid).

Secondly find local-setting.js file and copy (or merge the content) into "C:\program files\mozilla firefox\defaults\pref\local-setting.js"
The content of this file effectively says Mozilla to use configurations from another file mozilla.cfg

The approach was taken from https://github.com/kee-org/browser-addon/issues/55

To use autosubstitue in Google Takeout please save 2 identical passwords with different variations of the same username of https://accounts.google.com : one for pure your_google_username and the second one for username with google domain (like your google mail adress): your_google_username@gmail.com.
If you save only one of them then in some cases Firefox may fail to substitute passwords (it happened for me already during usage of GoogleTakeout when only one password was saved. Firefox just could not substitute. But it started to substitute when I saved another variation of my username).


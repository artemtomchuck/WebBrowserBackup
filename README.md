# WebBrowserBackup

*Purpose*
create backups from different online services using Selenium for emulating that backups is done by real user.
In ideal case some service can have some API which would allow you to just call one-line code Service.GetBackup() and get your backup.
But the reason for creating such script is that not every service has API that you can leverage to write/schedule automatic backup (however, recently Google added some schedule feature to their Google Takeout, but the frequency of backup is not flexible enough and Google Takeout schedule is limited by number of times the backup can be automatically executed).
So this script is intended to run the automatic backup for services that allow exporting user data via web browser interface.

*Features*
As of new the following services are supported for running the backup:
* GoogleKeep
currently Google data will be backed up into Dropbox account (does not make a lot of sense to make backup of Google service into Google drive, but you can adjust script to do this also)
* zenmoney.ru
backup for this service is just downloaded as output csv file. You can setup to upload the downloaded file to some service of online data storage (Dropbox, Google Drive etc) - for this additional development is needed to transfer output file from Downloads folder to your destination.
Also if it will not interfere with your other browser use-cases then you can map Browser download folder as a mirror to some online data storage service.



*Setup*
For initial setup please check also content of BrowserBackup\configuration folder

If you want to have backup of the same service, but with different accounts (e.g. backup for personal Google account and working Google account) then it can be achieved by creating separate Firefox profiles: one for personal profile with personal Google login and another one for working profile with working Google login.

You may want to schedule this backup. To do so you can use standard Windows scheduler (e.g. set daily backup or backup which is triggered when computer was started)

*Usage*
See usage guide by running
javaw -jar WebBrowserBackup.jar -help

Each backup call will produce it own log.

By default the backup works in headless mode so Firefox window will be invisible for the end user.


*Notes*
Please note that currently this script assumes that user has Russian language in his services (e.g. in Google).
So script will try to find buttons with Russian name in it.
If you want to have other language support then please change navigation buttons in script to your used language.
Theoretically you can rely on html tags while searching the buttons and this would make script suitable for each language.
But I am reluctant to this approach because html tags is a subject to constant changes (e.g. Google frequently updates their UI). And when you need to adjust this script due to service layout change then it is easiser to understand how it worked before by reading buttons names instead of html-tags.

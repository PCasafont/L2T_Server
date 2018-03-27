# L2T Server Project

L2Tenkai's L2j server fork, based on its Interlude version (started 2008) and having stopped synchronization on their Freya release.

## Getting Started


### Prerequisites

* [Intellij IDEA](https://www.jetbrains.com/idea/download)
* [MySQL Server](https://dev.mysql.com/downloads/mysql)

### Installing

* Clone the project
* Open it with IntelliJ IDEA. Make sure to import it as a Gradle project!
* Run the [database installer script](dist/tools/database_installer.bat)
* Create or modify a configuration file. They are the .properties files located at the [config folder](dist/config).
* Make the [global config file](dist/config.cfg) point to your configuration file.

### Running locally

There are a couple shared run configurations for IntelliJ, feel free to use them.

### Deploying

You can run either `gradle distZip` to generate a zip with all the necessary files to deploy or `gradle installDist` to generate a folder with all the necessary files to deploy.

## Authors

* **L2j Team**
* **Pere** - *Main fork author* - [PCasafont](https://github.com/PurpleBooth)
* **Lastravel** - *Contributed for 5 years* - [lastravel](https://github.com/lastravel)
* **Inia** - *New project maintainer* - [Inia68](https://github.com/Inia68)

Other contributors:
* Xavi - [XaviMorenoM](https://github.com/XaviMorenoM)
* Soul
* Kenkon
* Ghost

## License

This project is licensed under the GNU GPL License - see the [LICENSE.md](LICENSE.md) file for details.

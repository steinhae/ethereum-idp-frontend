# Repairchain Frontend
The idea for Repairchain was developed during a workshop organized by the [Blockchain Research Group at Technical University of Munich (TUM)](https://www.blockchain.tum.de "Blockchain Research Group at TUM"). Contact person and project supervisors are [Dr. Andranik Tumasjan](mailto:andranik.tumasjan@tum.de) and [Marcus Müller](mailto:marcus.mueller@tum.de). The project was realized in context of the interdisciplinary project in coorperation with the Department of Informatics And Mathematics by [Anton Widera, Hannes Steinhäuser and Joseph Palckal.](#authors).

The scope of this project was to develop a proof of concept DApp consisting of a frontend in form of an Android app and a backend consisting of smart contracts deployed to the Ethereum Blockchain. This repositiory contains the Android app.
<details>
  <summary>Click to expand detailed description</summary>

The idea behind Repairchain is that citizen can report
all kinds of issues in a city. Issues could be damaged road infrastructure, broken street lamps or any other kind of damages. Participating users can either confirm or reject these damage reports to have a crowdsourced administration of the issues. 
There is an existing app called SeeClickFix that offers similar functionalities with some
major disadvantages emerging from standard client/server architecture. Repairchain will
manage those by using blockchain technology and smart contracts. The following points are
addressed: 
* No need for a third party / intermediate &#8594; eliminates single point of failure and
reduces the cost
* Integrity by non-editable log &#8594; issues cannot be ignored/deleted by the city and must be handled
* Easy payment / rewards &#8594; sending funds in cryptocurrency is a built-in feature of
most Blockchains
* User are privileged &#8594; less administrative effort
* High automation possible &#8594; less personal needed, faster execution of operations

A project documentation can be found here INSERT LINK TO PDF.

</details>

## Getting Started

These instructions will get you a copy of the project up and running on your local machine for development and testing purposes.

### Prerequisites

* Android device or emulator
	* Developed for Android 7.1 (API level 25)​
	* Minimum is Android 5.0 (API level 21)​
* Deployed [Repairchain contract](https://github.com/steinhae/ethereum-idp-backend) (e.g. deployed to Ropsten testnet), for instructions check out the [project page](https://github.com/steinhae/ethereum-idp-backend)
* A geth instance connected to the Blockchain (same chain as deployed Repairchain contract)
	* Running on localhost or any server
	* Using a cloud hosted geth instance (e.g. via https://infura.io/).


### Installing

The first step is to create a configuration file named *local.properties* in *Repairchain/app/src/main/res/raw/*. The file contains a key *credentials_json*, the value is an encoded wallet in json format. A wallet file can be encrypted with the following tool [ethereum-wallet-decryptor](https://github.com/steinhae/ethereum-wallet-decryptor/releases).
The value of the other key *web3j_connection_url* points to the URL where the geth instance is running.

```
credentials_json={"address":"0x","ecKeyPair":{"privateKey":xxx,"publicKey":xxx}}
web3j_connection_url=https://URL:PORT
```

The next step is to build and run the app via Android Studio. 


## Built With


* [web3j](https://web3j.io/) - Java and Android library for integrating with nodes on Ethereum blockchains
* [Google Maps](https://maven.apache.org/) - Used to show reports on a map view
* [Butterknife](http://jakewharton.github.io/butterknife/) - Field and method binding for Android views
* [IPFSDroid](https://github.com/ligi/IPFSDroid) - Android implementation of the Inter Planetary File System (IPFS)

## Authors

* **Anton Widera** - *Master Student, Computer Science  @TUM* - [grillmann](https://github.com/grillmann)
* **Hannes Steinhäuser** - *Master Student, Computer Science  @TUM* - [steinhae](https://github.com/steinhae)
* **Joseph Palackal** - *Master Student, Computer Science  @TUM* - [palackjo](https://github.com/palackjo)

See also the list of [contributors](https://github.com/steinhae/ethereum-idp-frontend/contributors) who participated in this project.

## Supervisors

* [Dr. Andranik Tumasjan](https://www.strategy.wi.tum.de/en/people/phds-and-post-docs/dr-andranik-tumasjan/) - *Senior Research Director @ Blockchain Group TUM* - [email contact](mailto:andranik.tumasjan@tum.de)
* [Marcus Müller](https://www.strategy.wi.tum.de/en/people/phds-and-post-docs/marcus-mueller/) - *Research Director @ Blockchain Group TUM* - [email contact](mailto:marcus.mueller@tum.de)

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details

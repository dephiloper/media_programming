"use strict";

(function () {
    const Controller = de_sb_messenger.Controller;

    const MessageController = function () {
        Controller.call(this);
    };
    MessageController.prototype = Object.create(Controller.prototype);
    MessageController.prototype.constructor = MessageController;

    /**
     * Displays the associated view.
     */
    Object.defineProperty(MessageController.prototype, "display", {
        enumerable: false,
        configurable: false, // TODO attributes ripped from welcome controller
        writable: true,
        value: function () {
            // Controller.sessionOwner = null;
            // Controller.entityCache.clear();
            //
            // const mainElement = document.querySelector("main");
            // mainElement.appendChild(document.querySelector("#login-template").content.cloneNode(true).firstElementChild);
            // mainElement.querySelector("button").addEventListener("click", event => this.login());

            /**
            * TODO
                Die Instanz-Methode display() stellt diese View teilweise dar, und registriert die
                Methode displayMessageEditor() als Callback für das Klicken auf einen der
                Benutzer-Avatare im Avatar-Slider, zur Erzeugung einer Nachricht mit der gewählten
                Person als subject. Des Weiteren wird die Methode displayRootMessages()
                aufgerufen um die Darstellung der Seite zu vervollständigen.
            */
        }
    });

    Object.defineProperty(MessageController.prototype, "displayMessages", {
        enumerable: false,
        configurable: false, // TODO attributes ripped from preferences display Session owner because idk
        value: function () {
            /**
            * TODO
                Die Instanz-Methode displayMessages() soll gegebene Nachrichten als Kinder des
                gegebenen DOM-Elements anzeigen. Ein Klick auf das Plus-Symbol neben dem AutorAvatar
                einer Nachricht soll die Methode toggleChildMessages() aufrufen. Ein
                Klick auf einen der Avatare soll dagegen die Methode displayMessageEditor()
                aufrufen um eine Nachricht zu erzeugen welche die gewählte Nachricht als subject
                assoziiert.
            */
        }
    });

    Object.defineProperty(MessageController.prototype, "displayRootMessages", {
        enumerable: false,
        configurable: false, // TODO attributes ripped from preferences display Session owner because idk
        value: function () {
            /**
            * TODO
                Die Instanz-Methode displayRootMessages() soll diejenigen Nachrichten
                untereinander anzeigen welche als subject (sic!) entweder den aktuellen Benutzer oder
                eine von diesem beobachtete Person haben. Die Nachrichten werden dabei mittels GET
                /services/entities/{id}/messagesCaused abgefragt, gesammelt, und im Section-Element
                mit der CSS-Klasse „messages“ mittels displayMessages() angezeigt.
            */
        }
    });

    Object.defineProperty(MessageController.prototype, "toggleChildMessages", {
        enumerable: false,
        configurable: false, // TODO attributes ripped from preferences display Session owner because idk
        value: function () {
            /**
             * TODO
                 Die Instanz-Methode toggleChildMessages() soll eine NachrichtenHierarchieebene
                 entweder ein- oder ausblenden, je nachdem ob sie bereits ein- oder
                 ausgeblendet ist. Wird eine Hierarchieebene eingeblendet, dann sollen diejenigen
                 Nachrichten welche die Parent-Message als subject besitzen mittels REST abgefragt,
                 und mittels displayMessages() zur Anzeige gebracht werden. Zum Ausblenden soll
                 displayMessages() dagegen mit einer leeren Message-Menge aufgerufen werden.
             */
        }
    });

    Object.defineProperty(MessageController.prototype, "displayMessageEditor", {
        enumerable: false,
        configurable: false, // TODO attributes ripped from preferences display Session owner because idk
        value: function (parentElement, subjectIdentity) {
            /**
             * TODO
                 Die Instanz-Methode displayMessageEditor(parentElement, subjectIdentity)
                 soll einen Nachrichten-Editor auf der gewählten Hierarchieebene einblenden. Die
                 Nachricht soll dabei das gegebene subject, und den aktuellen Benutzer als author
                 erhalten. Bei Klick auf deren Sende-Knopf soll die Nachricht mittels
                 persistMessage() gespeichert, und die Hierarchieebene frisch geladen werden.
             */
        }
    });

    Object.defineProperty(MessageController.prototype, "persistMessage", {
        enumerable: false,
        configurable: false, // TODO attributes ripped from preferences display Session owner because idk
        value: function (messageElement, subjectIdentity) {
            /**
            * TODO
                Soll eine neue Nachricht mittels REST-Call speichern.
            */
        }
    });


}());
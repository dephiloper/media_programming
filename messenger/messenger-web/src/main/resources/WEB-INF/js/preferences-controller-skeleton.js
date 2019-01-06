/**
 * de_sb_messenger.PreferencesController: messenger preferences controller.
 * Copyright (c) 2013 Sascha Baumeister
 */
"use strict";

(function () {
	// imports
	const Controller = de_sb_messenger.Controller;
	const URL = window.URL || window.webkitURL;


	/**
	 * Creates a new preferences controller that is derived from an abstract controller.
	 */
	const PreferencesController = function () {
		Controller.call(this);
	}
	PreferencesController.prototype = Object.create(Controller.prototype);
	PreferencesController.prototype.constructor = PreferencesController;


	/**
	 * Displays the associated view.
	 */
	Object.defineProperty(PreferencesController.prototype, "display", {
		enumerable: false,
		configurable: false,
		writable: true,
		value: function () {
			if (!Controller.sessionOwner) return;
			this.displayError();

			try {
				const mainElement = document.querySelector("main");
				mainElement.appendChild(document.querySelector("#preferences-template").content.cloneNode(true).firstElementChild);
				mainElement.querySelector("button").addEventListener("click", event => this.persistSessionOwner());

				const imageElement = mainElement.querySelector("img");
				imageElement.addEventListener("dragover", event => event.preventDefault());
				imageElement.addEventListener("drop", event => {
					event.preventDefault();
					if (event.dataTransfer.files.length > 0) {
						const file = event.dataTransfer.files[0];
						event.target.src = URL.createObjectURL(file);
						this.persistAvatar(file);
					}
				});
				imageElement.addEventListener("load", event => {
					event.preventDefault();
					URL.revokeObjectURL(event.target.src);
				});

				this.displaySessionOwner();
			} catch (error) {
				this.displayError(error);
			}	
		}
	});


	/**
	 * Displays the session owner.
	 * Note artificial use of changing time parameter to bypass browser caching
	 */
	Object.defineProperty(PreferencesController.prototype, "displaySessionOwner", {
		enumerable: false,
		configurable: false,
		value: function () {
			const sectionElement = document.querySelector("section.preferences");
			const activeElements = sectionElement.querySelectorAll("input, img");
			const sessionOwner = Controller.sessionOwner;
			activeElements[0].src = "/services/people/" + sessionOwner.identity + "/avatar?cache-bust=" + Date.now();
			activeElements[1].value = sessionOwner.group;
			activeElements[2].value = sessionOwner.email;
			activeElements[3].value = "";
			activeElements[4].value = sessionOwner.name.given;
			activeElements[5].value = sessionOwner.name.family;
			activeElements[6].value = sessionOwner.address.street;
			activeElements[7].value = sessionOwner.address.postcode;
			activeElements[8].value = sessionOwner.address.city;
		}
	});


	/**
	 * Persists the session owner.
	 */
	Object.defineProperty(PreferencesController.prototype, "persistSessionOwner", {
		enumerable: false,
		configurable: false,
		value: async function () {
			this.displayError();

			try {
				const sectionElement = document.querySelector("section.preferences");
				const inputElements = sectionElement.querySelectorAll("input");

				const clone = JSON.parse(JSON.stringify(Controller.sessionOwner));
				clone.name.given = inputElements[3].value.trim();
				clone.name.family = inputElements[4].value.trim();
				clone.address.street = inputElements[5].value.trim();
				clone.address.postcode = inputElements[6].value.trim();
				clone.address.city = inputElements[7].value.trim();
				delete clone.peopleObservedReferences;
				delete clone.peopleObservingReferences;

				const body = JSON.stringify(clone);
				const password = [inputElements[2].value.trim()].find(value => value != "");
				const email = password ? clone.email : undefined;

				const headers = {"Content-Type": "application/json"};
				if (password) headers["Set-password"] = password;
				const response = await fetch("/services/people", { method: "POST", headers: headers, body: body, credentials: "include" });
				if (!response.ok) throw new Error("HTTP " + response.status + " " + response.statusText);

				// fetch() supports only sending credentials from a browser's hidden Basic-Auth credentials store, not
				// storing them. This workaround uses one classic XMLHttpRequest invocation to circumvent this problem.
				Controller.sessionOwner = JSON.parse(await Controller.xhr("/services/people/requester", "GET", {"Accept": "application/json"}, "", "text", email, password));
				this.displaySessionOwner();
			} catch (error) {
				if (error instanceof Error && error.message.startsWith("HTTP 409")) {
					Controller.welcomeController.display(); 
				} else {
					this.displaySessionOwner();
				}

				this.displayError(error);
			}
		}
	});


	/**
	 * Persists the session owner's avatar.
	 * @param {File} avatarFile the avatar file
	 */
	Object.defineProperty(PreferencesController.prototype, "persistAvatar", {
		enumerable: false,
		configurable: false,
		value: async function (avatarFile) {
			const imageElement = document.querySelector("section.preferences img");
			this.displayError();

			// TODO: call PUT /services/people/{id}/avatar" to store the given avatar file, using
			// either fetch() or Controller.xhr(). Throw an exception if the call fails. If it
			// succeeds, increment the version of Controller.sessionOwner by 1. In case of an error,
			// call this.displayError(error). In any case, alter the src-property of the imageElement
			// to "/services/people/{id}/avatar?cache-bust=" + Date.now() in order to bypass the
			// browser's image cache and display the modified image.
		}
	});


	/**
	 * Perform controller callback registration during DOM load event handling.
	 */
	window.addEventListener("load", event => {
		const anchor = document.querySelector("header li:nth-of-type(4) > a");
		const controller = new PreferencesController();
		anchor.addEventListener("click", event => controller.display());
	});
} ());
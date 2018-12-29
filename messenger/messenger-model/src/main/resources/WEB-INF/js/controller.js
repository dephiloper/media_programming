/**
 * de_sb_messenger.Controller: abstract controller.
 * Copyright (c) 2013 Sascha Baumeister
 */
"use strict";

this.de_sb_messenger = this.de_sb_messenger || {};
(function () {

	/**
	 * Creates an "abstract" controller.
	 */
	const Controller = de_sb_messenger.Controller = function () {}


	/**
	 * Displays the view associated with this controller.
	 */
	Object.defineProperty(Controller.prototype, "display", {
		enumerable: false,
		configurable: false,
		writable: true,
		value: function () {
			throw new Error("this operation must be overriden!");
		}
	});


	/**
	 * Displays the given error in the footer, or resets it if none is given.
	 * @param error {Object} the optional error
	 */
	Object.defineProperty(Controller.prototype, "displayError", {
		enumerable: false,
		configurable: false,
		writable: true,
		value: function (error) {
			const outputElement = document.querySelector("body > footer output");
			if (error) {
				console.error(error);
				outputElement.value = error instanceof Error ? error.message : error;
			} else {
				outputElement.value = "";
			}
		}
	});


	/**
	 * Sends an asynchronous HTTP request, and returns a promise that resolves into an HTTP
	 * response body. The promise is rejected if there is a network I/O problem, or if the
	 * response's status is out of the [200,299] success range; the resulting exception
	 * carries the (raw) HTTP response headers in an additional "headers" property.
	 * @param resource {String} the service URI
	 * @param method {String} the service method
	 * @param headers {Object} the optional request headers map
	 * @param body {Object} the optional request body
	 * @param type {String} the optional response type ("text", "arraybuffer", "blob", ...) 
	 * @param alias {String} an optional user alias
	 * @param password {String} an optional user password
	 * @return {Promise} the promise of a resolved XMLHttpRequest
	 * @throws {Error} if HTTP authentication fails
	 */
	 Object.defineProperty(Controller.prototype, "xhr", {
		enumerable: false,
		configurable: false,
		value: function (resource, method, headers, body, type, alias, password) {
			const exchange = new XMLHttpRequest();
			exchange.responseType = type || "text";
			exchange.withCredentials = true;
			exchange.open(method, resource, true, alias, password);
			for (const key in Object.keys(headers || {})) {
				exchange.setRequestHeader(key, headers[key]);
			}

			return new Promise((resolve, reject) => {
				exchange.addEventListener("load", event => {
					if (event.target.status >= 200 && event.target.status <= 299) {
						resolve(event.target.response);
					} else {
						const error = new Error("HTTP " + event.target.status + " " + event.target.statusText);
						error.headers = event.target.getAllResponseHeaders();
						reject(error);
					}
				});

				exchange.addEventListener("error", event => reject(new Error("HTTP exchange failed")));
				exchange.send(body || "");
			});
		}
	});


	/**
	 * Refreshes the content of the given image slider with the avatars of
	 * the given people. Note that the given callback function is expected
	 * to be a method of this controller, designed to receive the identity
	 * of the person clicked as a parameter.
	 * @param sliderElement {Element} the HTML element representing the image slider
	 * @param personIdentities {Array} the identities of the people to be represented
	 * @param clickAction {Function} callback function to be called when an icon is clicked,
	 *								 receiving a person as argument
	 * @throws TypeError if any of the arguments is illegal or null/undefined
	 */
	Object.defineProperty(Controller.prototype, "refreshAvatarSlider", {
		enumerable: false,
		configurable: false,
		value: async function (sliderElement, personIdentities, clickAction) {
			if (!(sliderElement instanceof Element) || !(personIdentities instanceof Array) || typeof clickAction != "function") throw new TypeError();
			while (sliderElement.lastChild) sliderElement.removeChild(sliderElement.lastChild);
	
			for (const personIdentity of personIdentities) {
				const person = await Controller.entityCache.get(personIdentity);

				const imageElement = document.createElement("img");
				imageElement.src = "/services/people/" + person.identity + "/avatar";

				const anchorElement = document.createElement("a");
				anchorElement.appendChild(imageElement);
				anchorElement.appendChild(document.createTextNode(person.name.given));
				anchorElement.title = person.name.given + " " + person.name.family;
				anchorElement.addEventListener("click", event => clickAction(person));
				sliderElement.appendChild(anchorElement);
			}
		}
	});


	/**
	 * The globally accessible session owner entity.
	 */
	Object.defineProperty(Controller, "sessionOwner", {
		enumerable : false,
		configurable : false,
		writable : true,
		value : null
	});


	/**
	 * The globally accessible entity cache.
	 */
	Object.defineProperty(Controller, "entityCache", {
		enumerable: false,
		configurable: false,
		value: new de_sb_util.EntityCache("/services/entities")
	});


	/**
	 * Private operation handling menu item selection and main element clearance.
	 * @param {Event} event the click event
	 */
	const clickAction = function (event) {
		const mainElement = document.querySelector("main");
		while (mainElement.lastChild) {
			mainElement.removeChild(mainElement.lastChild);
		}

		const menuElement = event.target.parentElement;
		for (const element of menuElement.parentElement.children) {
			element.classList.remove("selected");
		}
		menuElement.classList.add("selected");
	};


	/**
	 * Perform controller callback registration during DOM load event handling.
	 * The callbacks handle menu item selection and main element clearance.
	 */
	window.addEventListener("load", event => {
		for (const anchor of document.querySelectorAll("header a")) {
			anchor.addEventListener("click", clickAction);
		}
	});
} ());
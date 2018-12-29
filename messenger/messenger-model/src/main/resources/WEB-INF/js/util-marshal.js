/**
 * de_sb_util:
 * - XML_MARSHALER singleton:	marshaler for application/xml data 
 * - FORM_MARSHALER singleton:	marshaler for application/x-www-form-urlencoded data 
 * Copyright (c) 2013 Sascha Baumeister
 */
"use strict";

this.de_sb_util = this.de_sb_util || {};
(function () {

	/**
	 * Creates the XML_MARSHALER singleton for marshaling application/xml data.
	 * The XML format specifics chosen are compatible with the MOXY XML marshaler,
	 * especially regarding synthetic type properties and @-prefixed attribute names.
	 */
	de_sb_util.XML_MARSHALER = new function () {
		const DOC_DECLARATION = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>";

		/**
		* Recursively marshals the given object into an XML document. The result will contain
		* the given root element, except if the given object is null, an array, or a function.
		* Any primitive typed field (strings, numbers, booleans) is marshaled into an attribute.
		* Any array typed field is marshaled into multiple child elements, while any other
		* Object type is recursively marshaled into a single child element. Note that similarly
		* to JSON.stringify(), joint references to the same child object will be represented
		* as content equal but disjoint XML elements. Also, recursive object references are
		* not supported, and will cause infinite loops.
		* @param {Object} object the object to be marshaled
		* @param {String} rootElementName the root element name for the given object
		* @return {String} the corresponding XML document text
		* @throws {TypeError} if the given object is null, a function or an array
		*/
		Object.defineProperty(this, "marshal", {
			configurable: false,
			enumerable: false,
			value: function (object, rootElementName) {
				if (object == null || typeof object == "function" || object instanceof Array) throw new TypeError("illegal argument");

				const dom = document.implementation.createDocument(null, rootElementName);
				recursiveMarshal(object, dom.documentElement);

				const serializer = new XMLSerializer();
				return DOC_DECLARATION + serializer.serializeToString(dom);
			}
		});


		/**
		* Recursively unmarshals the given XML text into an object. If the XML root element
		* contains only text, the latter is returned. Otherwise, a generic object is
		* assembled that contains fields named after each element's attributes and child
		* elements. If an element contains multiple child elements sharing the same name,
		* their values are joined into an array. Note that this implies that field values
		* may be undefined (zero occurrences), object types (single occurrence), or array
		* types (multiple occurrences) depending on the XML content.
		* @param {String} xml the XML document text to be unmarshaled
		* @return {Object} the corresponding object
		*/
		Object.defineProperty(this, "unmarshal", {
			configurable: false,
			enumerable: false,
			value: function (xml) {
				const dom = new DOMParser().parseFromString(xml, "text/xml");
				const rootElement = dom.documentElement;
				const object = rootElement.attributes.length === 0 && rootElement.children.length === 0
					? {}
					: recursiveUnmarshal(rootElement);
				object.type = rootElement.nodeName;
				return object;
			}
		});


		/**
		* Private function recursively marshaling the given object's properties into the
		* given XML element. Any property with an @-prefixed name is marshaled into an
		* attribute. Any array typed field is marshaled into multiple child elements,
		* while any other object type is recursively marshaled into a single child element.
		* Note that similarly to JSON.stringify(), joint references to the same child
		* object will be represented as content equal but disjoint XML elements. Also,
		* recursive object references are not supported, and will cause infinite loops.
		* Finally, the given element is expected to be owned by a document.
		* @param {Object} object the object to be marshaled
		* @param {Element} element the resulting DOM element
		*/
		function recursiveMarshal (object, element) {
			const type = Object.prototype.toString.call(object);
			if (type == "[object String]" || type == "[object Number]" || type == "[object Boolean]") {
				const node = element.ownerDocument.createTextNode(object);
				element.appendChild(node);
			} else {
				for (; object != null; object = Object.getPrototypeOf(object)) {
					for (const key in object) {
						const value = object[key];
						if (value == null || typeof value == "function") continue;

						if (key.startsWith("@")) {
							const node = element.ownerDocument.createAttribute(key.substring(1));
							node.value = value;
							element.setAttributeNode(node);
						} else {
							const values = value instanceof Array ? value : [value];
							for (const object of values) {
								const node = element.ownerDocument.createElement(key);
								element.appendChild(node);
								recursiveMarshal(object, node);
							}
						}
					}
				}
			}
		}


		/**
		* Private function recursively unmarshaling the given DOM element into an object. If
		* the node is a text node, it's text value is returned. Otherwise a generic object is
		* returned that contains fields named after the node's attributes and child elements.
		* If a node contains multiple child elements sharing the same name, they are joined
		* into an array.
		* @param {Element} element the DOM element to be unmarshaled
		* @return {Object} the resulting object
		*/
		function recursiveUnmarshal (element) {
 			if (element.attributes.length === 0 && element.children.length === 0) return element.textContent;

			const object = {};
			for (const attribute of element.attributes) {
				object["@" + attribute.nodeName] = attribute.nodeValue;
			}

			for (const child of element.children) {
				const key = child.nodeName;
				const value = recursiveUnmarshal(child);

				if (key in object) {
					const existingValue = object[key];
					if (existingValue instanceof Array) {
						existingValue.push(value);
					} else {
						object[key] = [existingValue, value];
					}
				} else {
					object[key] = value;
				}
			}

			return object;
		}
	}



	/**
	 * Creates the URL_FORM singleton for marshaling application/x-www-form-urlencoded
	 * data. The objects to be produced/consumed may contain multiple properties, each
	 * of which either has a primitive scalar type (String, Number, Boolean), or an
	 * array type filled with primitive scalar elements.
	 */
	de_sb_util.FORM_MARSHALER = new function () {

		/**
		 * Marshals the given form object and returns the resulting text.
		 * @param object {Object} the form object
		 * @return {String} the corresponding form text representation
		 * @throws {TypeError} if the given object is null, a function or an array
		 */
		Object.defineProperty(this, "marshal", {
			configurable: false,
			enumerable: false,
			value: function (object) {
				if (object == null || typeof object == "function" || object instanceof Array) throw new TypeError("illegal argument");

				const entries = [];
				for (const key in object) {
					const prefix = encodeURIComponent(key);
					const value = object[key];
					if (value == null) {
						entries.push(prefix);
					} else if (value instanceof Array) {
						for (const element of value) {
							entries.push(prefix + "=" + encodeURIComponent(element));
						}
					} else {
						entries.push(prefix + "=" + encodeURIComponent(value));
					}
				}
				return entries.join("&");				 
			}
		});


		/**
		 * Unmarshals the given form text and returns the resulting object.
		 * @param text {String} the form text
		 * @return {Object} the corresponding form object representation
		 */
		Object.defineProperty(this, "unmarshal", {
			configurable: false,
			enumerable: false,
			value: function (text) {
				const object = {};
				text.split("&").forEach(entry => {
					const offset = entry.indexOf("=");
					const key = decodeURIComponent(entry.substring(0, offset === -1 ? entry.length : offset));
					const value = offset === -1 ? null : decodeURIComponent(entry.substring(offset + 1, entry.length));
					if (key in object) {
						if (typeof object[key] != "array") object[key] = [object[key]];
						object[key].push(value);
					} else {
						object[key] = value;
					}
				});
				return object;
			}
		});
	}
} ());
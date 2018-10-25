/**
 * de_sb_util:
 * - EntityCache: REST based entity cache
 * Copyright (c) 2013 Sascha Baumeister
 */
"use strict";

this.de_sb_util = this.de_sb_util || {};
(function () {


	/**
	 * Creates a new entity cache instance for the given requestURI. The cache
	 * will assume that the entities can be retrieved from this URI using a
	 * RESTful GET request that returns the entity encoded in content-type
	 * "application/json", and that they contain a key named "@identity".
	 * @param requestURI {String} the request URI
	 */
	const EntityCache = de_sb_util.EntityCache = function (requestURI) {
		Object.defineProperty(this, "requestURI", {
			enumerable: true,
			configurable: false,
			writable: false,
			value: requestURI
		});

		Object.defineProperty(this, "content", {
			enumerable: true,
			configurable: false,
			writable: false,
			value: {}
		});
	}


	/**
	 * Returns a promise for the entity with the given identity. If the entity required
	 * is cached, this results in an immediately resolving promise. Otherwise the promise
	 * returned resolves asynchronously after performing a REST service call to retreive
	 * the entity.
	 * @param entityIdentity {Object} the entity identity
	 * @return {Promise} the promise of a corresponding entity
	 */
	Object.defineProperty(EntityCache.prototype, "get", {
		configurable: false,
		enumerable: false,
		value: async function (entityIdentity) {
			const key = entityIdentity.toString();
			if (key in this.content) return this.content[key];

			const response = await fetch(this.requestURI + "/" + key, { method: "GET", headers: {"Accept": "application/json"}, credentials: "include" });
			if (!response.ok) throw new Error("HTTP " + response.status + " " + response.statusText);
			return this.content[key] = await response.json();
		}
	});


	/**
	 * Adds the given entity to this cache.
	 * @param entity {Object} the entity
	 */
	Object.defineProperty(EntityCache.prototype, "put", {
		configurable: false,
		enumerable: false,
		value: function (entity) {
			this.content[entity.identity.toString()] = entity;
		}
	});


	/**
	 * Removes the given entity from this cache (if it exists).
	 * @param entityIdentity {Object} the entity identity
	 */
	Object.defineProperty(EntityCache.prototype, "remove", {
		configurable: false,
		enumerable: false,
		value: function (entityIdentity) {
			delete this.content[entityIdentity.toString()];
		}
	});


	/**
	 * Clears this cache.
	 */
	Object.defineProperty(EntityCache.prototype, "clear", {
		configurable: false,
		enumerable: false,
		value: function () {
			for (const key in this.content) {
				delete this.content[key];
			}
		}
	});
} ());
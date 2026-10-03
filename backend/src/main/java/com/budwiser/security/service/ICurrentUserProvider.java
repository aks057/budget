package com.budwiser.security.service;

/**
 * The ONLY source of the caller's identity. Never take a user id from request params, bodies, or LLM tool args.
 */
public interface ICurrentUserProvider {

  Long getUserId();
}

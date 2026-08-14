package com.inscopelabs.abx.clipinbox.cbxdag.architecture

/**
 * Live CBX-LINK Cloudflare Worker endpoint. Deployed and verified
 * end-to-end (delegation creation, status read-back, SQLite persistence)
 * via curl from Termux in Dynamic DAG POC Phase P0.
 */
object CbxLinkConfig {
    const val BASE_URL = "https://cbx-link.cbx-dag.workers.dev"
    const val DELEGATIONS_PATH = "/v1/delegations"
}

<h1 align="center"><a href="#">Decentralized Identity Management for Web Applications Using Blockchain</a></h1>

<p align="center">
  <img src=""  height="200"/>
  <br><br>
  <i>A decentralized identity federation for web applications,<br>
  enabling secure, user-controlled cross-border logins using blockchain and dynamic trust discovery.
  <br><br>
  </i>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/JavaScript-F7DF1E?style=for-the-badge&logo=javascript&logoColor=black" alt="javascript"/>
  <img src="https://img.shields.io/badge/TypeScript-3178C6?style=for-the-badge&logo=typescript&logoColor=white" alt="typescript"/>
  <img src="https://img.shields.io/badge/Java-007396?style=for-the-badge&logo=openjdk&logoColor=white" alt="java"/>
</p>
<p align="center">
  <img src="https://img.shields.io/badge/Node.js-339933?style=for-the-badge&logo=nodedotjs&logoColor=white" alt="nodejs"/>
  <img src="https://img.shields.io/badge/Next.js-000000?style=for-the-badge&logo=next.js&logoColor=white" alt="next.js"/>
<img src="https://img.shields.io/badge/Spring Boot-6DB33F?style=for-the-badge&logo=springboot&logoColor=white" alt="spring boot"/>
</p>
<p align="center">
<img src="https://img.shields.io/badge/MongoDB-47A248?style=for-the-badge&logo=mongodb&logoColor=white" alt="mongodb"/>
  <img src="https://img.shields.io/badge/MySQL-4479A1?style=for-the-badge&logo=mysql&logoColor=white" alt="mysql"/>
  <img src="https://img.shields.io/badge/PostgreSQL-4169E1?style=for-the-badge&logo=postgresql&logoColor=white" alt="postgresql"/>
  </p>
<p align="center">
    <img src="https://img.shields.io/badge/Ethereum-3C3C3D?style=for-the-badge&logo=ethereum&logoColor=white" alt="ethereum"/>
    <img src="https://img.shields.io/badge/OIDC-FF0077?style=for-the-badge&logo=openid&logoColor=white" alt="oidc"/>
</p>

## About the Project

This project demonstrates a **new approach to federated identity** for web applications.  
Today, users often rely on big tech social logins or static government identity systems that require complex peering and offer limited user control.  
This system replaces static trust relationships with **dynamic discovery**, powered by a Cross-Identity Gateway and a network of independent **Country Nodes**.

A modern **blockchain layer** anchors identity proofs publicly and verifiably, without exposing sensitive user data.  
This prototype combines:
- A **Cross-Identity Gateway** that uses Decentralized Identifiers (DIDs) to find a user’s home node automatically.
- **Country Nodes**, which store registered applications and act as both Application Nodes (managing app info) and Identity Nodes (handling secure authentication).
- **Identity Providers** that verify credentials within each country, issuing scoped identity tokens.
- A **blockchain-anchored flow** (in Orange Country) that writes cryptographic hashes to Ethereum Sepolia for tamper resistance.

Together, these parts show how a **flexible, privacy-respecting, cross-border identity network** can work in practice.

##  How It Works

- Users sign in to an app. The **Application Node** contacts the **Cross-Identity Gateway**, which resolves the user’s DID to find their home Country Node.
- For **same-country logins**, the local Identity Provider verifies credentials and the local Application Node issues tokens directly.
- For **cross-border logins**, the foreign node proxies the request to the home Country Node, which authenticates the user and issues a secure, scoped token.
- In Orange Country, the system writes a hash of the user’s identity proof to **Ethereum Sepolia**, providing a public, tamper-proof record.
- Banana Country shows how a legacy OAuth2 provider can run alongside modern blockchain flows, proving the system’s backward compatibility.

## Features

-  **Dynamic Cross-Border Federation** — No static peering. Discovery and trust are handled at runtime.
-  **User-Controlled Authentication** — Users always authenticate at their home node. No credentials are shared directly with foreign apps.
-  **Blockchain Anchoring** — Hashes on Ethereum Sepolia verify identity proofs without exposing raw data.
-  **Flexible Tech Stack** — Works with modern (blockchain-based) and legacy (OAuth2) Identity Providers.
-  **Supports Same-Country and Cross-Border Logins** — Handles both with a single system.

## Planned Improvements

-  **Remove Central Gateway** — Replace it with a fully decentralized blockchain-based discovery network to eliminate single points of failure and solve duplicate identity issues.
-  **Admin Panels** — Add easy-to-use dashboards for managing apps, scopes, and user consent in each Country Node.
-  **User Migration** — Build secure flows for migrating identities between Country Nodes when people move abroad.
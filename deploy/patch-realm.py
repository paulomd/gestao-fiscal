#!/usr/bin/env python3
"""Adiciona redirect URIs do host público ao realm Keycloak (pasta deploy/config)."""
import json
import sys
from pathlib import Path

REALM = Path(__file__).resolve().parent / "config" / "keycloak" / "realm-entrevista.json"


def add_unique(items: list, value: str) -> None:
    if value not in items:
        items.append(value)


def main() -> int:
    if len(sys.argv) != 2:
        print(f"Uso: {sys.argv[0]} http://host-ou-dominio", file=sys.stderr)
        return 1
    base = sys.argv[1].rstrip("/")
    data = json.loads(REALM.read_text(encoding="utf-8"))
    for client in data.get("clients", []):
        cid = client.get("clientId")
        if cid not in ("legacy-jsf", "angular-app"):
            continue
        add_unique(client.setdefault("redirectUris", []), f"{base}/*")
        add_unique(client.setdefault("webOrigins", []), base)
        if cid == "legacy-jsf":
            add_unique(
                client["redirectUris"],
                f"{base}/login/oauth2/code/keycloak",
            )
            attrs = client.setdefault("attributes", {})
            pl = attrs.get("post.logout.redirect.uris", "")
            extra = f"{base}/*##{base}/login-jsf"
            if extra not in pl:
                attrs["post.logout.redirect.uris"] = f"{pl}##{extra}" if pl else extra
    REALM.write_text(json.dumps(data, indent=2, ensure_ascii=False) + "\n", encoding="utf-8")
    print(f">>> Realm atualizado: {REALM}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())

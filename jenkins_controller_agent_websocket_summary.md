# Jenkins Controller ↔ Agent (Containerized, WebSocket, No SSH) — Summary

## **Architecture Overview**
- **Controller (VM A):** Jenkins controller container (exposed on `:8080` or behind HTTPS proxy).
- **Agent (VM B):** Jenkins inbound agent container (no exposed ports).
- **Connectivity:** Inbound **WebSocket** connection from agent → controller.  
  - No SSH required  
  - No TCP/50000 needed  
  - Both VMs can safely use port `8080` internally — no conflict.

---

## **Controller Setup (VM A)**

1. **Create a new node**
   - *Manage Jenkins → Nodes → New Node → Permanent Agent*  
   - Name: e.g. `vm-b-agent-1`  
   - Remote root dir: `/home/jenkins/agent`  
   - Launch method: **“Launch agent by connecting it to the controller”**  
   - Save → Copy the **agent secret** from the node page.

2. **Ensure Jenkins URL is correct**
   - *Manage Jenkins → System → Jenkins URL* = `https://jenkins-a.example.com`  
   - This must be reachable from VM B.

3. **Controller container example**
   ```yaml
   services:
     jenkins-controller:
       image: jenkins/jenkins:lts-jdk17
       container_name: jenkins-controller
       ports:
         - "8080:8080"
         # No need to expose 50000 with WebSocket
       volumes:
         - jenkins_home:/var/jenkins_home
   volumes:
     jenkins_home:
   ```

4. *(Optional)* If using NGINX/Traefik, ensure **WebSocket upgrades**:
   ```nginx
   proxy_set_header Upgrade $http_upgrade;
   proxy_set_header Connection "upgrade";
   proxy_http_version 1.1;
   ```

---

## **Agent Setup (VM B)**

### `docker-compose.yml`
```yaml
services:
  jenkins-inbound-agent:
    image: jenkins/inbound-agent:latest-jdk17
    container_name: jenkins-inbound-agent
    restart: unless-stopped
    environment:
      - JENKINS_URL=https://jenkins-a.example.com
      - JENKINS_SECRET=REPLACE_WITH_SECRET
      - JENKINS_AGENT_NAME=vm-b-agent-1
      - JENKINS_AGENT_WORKDIR=/home/jenkins/agent
    volumes:
      - agent-workdir:/home/jenkins/agent
      # Optional: enable Docker build support
      # - /var/run/docker.sock:/var/run/docker.sock
    command: >
      -url ${JENKINS_URL}
      -webSocket
      -workDir /home/jenkins/agent
volumes:
  agent-workdir:
```

### Notes
- The agent connects *outbound* via WebSocket — no open ports on VM B.
- Works fine if both sides use `:8080`; different hosts → no conflict.
- Container image includes `agent.jar` and Java runtime.

---

## **Firewall / UFW Rules**

**Controller (VM A)**
```bash
sudo ufw default deny incoming
sudo ufw default allow outgoing
sudo ufw allow 22/tcp
sudo ufw allow 80/tcp
sudo ufw allow 443/tcp
# No need for 50000 with WebSocket
sudo ufw enable
```

**Agent (VM B)**
- No inbound rules required.
- Ensure outbound connectivity to VM A’s port 8080/443.

---

## **Verification & Troubleshooting**
- Test connectivity from VM B:
  ```bash
  curl -I https://jenkins-a.example.com/tcpSlaveAgentListener/
  ```
  Expect `HTTP/200` with Jenkins headers.

- Common causes for “agent offline”:
  - Wrong `JENKINS_URL` or `SECRET`
  - Proxy not forwarding WebSocket upgrades
  - Jenkins URL mismatch in *Manage Jenkins → System → Jenkins URL*

- Once connected, node status in Jenkins → **Online**.

---

## **Optional Enhancements**
- Mount `/var/run/docker.sock` to allow Dockerized build steps.
- Use labels to route specific pipelines (`agent { label 'vm-b' }`).
- For persistence, use named volumes for `/home/jenkins/agent`.
- Configure NGINX/Traefik with long `proxy_read_timeout` for stability.

---

## **TL;DR**
✅ Controller in container on VM A (port 8080 or HTTPS 443)  
✅ Agent in container on VM B, using `jenkins/inbound-agent`  
✅ Connection via **WebSocket** (outbound only)  
✅ No SSH, no port 50000, no inbound rules on the agent side  

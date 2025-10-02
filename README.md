# EmpiresOfAlan
A premium, feature-rich nation and land management plugin for Minecraft servers running Paper 1.21.4.

## 📦 Version & Requirements
- **Version**: 1.0.0
- **Server**: Paper 1.21.4 or compatible fork
- **Java**: Java 21 or higher
- **License**: BSD 2-Clause

### Dependencies
- **Required**: [Vault](https://www.spigotmc.org/resources/vault.34315/) (Economy)
- **Optional**: [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/), [BlueMap](https://bluecolo.red/bluemap/)

---

## 🌟 Features

### 🏰 Town System
- Create and manage towns with customizable names and settings  
- Claim/unclaim land chunks with fine-grained permission controls  
- Town banks with economy integration (Vault support)  
- Resident management with role-based permissions: **Owner, Mayor, Knight, Member**  
- Town spawns with teleportation and protection  
- Customizable claim flags (PvP, explosions, mob spawning)  

### 🌍 Nation System
- Form powerful nations by uniting multiple towns  
- Nation banks and taxation systems  
- Diplomatic relations (alliances and enemies)  
- Nation hierarchies with **King, Officer, Knight, Member**  
- Nation spawns and teleportation networks  

### ⚔️ War System
- Declare wars between nations with configurable objectives  
- Multiple war types: territory capture, PvP dominance, resource control  
- Rewards and penalties system  
- Surrender mechanics and war duration controls  
- Detailed tracking and statistics  

### 🧨 Mercenary System
- Create and manage mercenary clans  
- Hire mercenary clans for temporary assistance  
- Contracts with configurable durations  
- Clan banks and economic management  
- Clan promotions and hierarchies  

### 💬 Advanced Chat System
- Channels: Local, Town, Nation, Alliance, Global  
- Spy/admin channels  
- Mute/toggle chat channels, anti-spam & word filtering  
- Rank & role-based prefixes  
- Color-coded chat messages  

### 🎨 Graphical User Interface
- Intuitive inventory-based GUIs  
- Claim visualizations  
- Nation diplomacy panels  
- War objectives tracking  
- Recruitment centers for mercenaries  
- Chat channel selector  

### 🔧 Technical Features
- SQLite database (async operations)  
- Event-driven architecture  
- Modular & optimized code  
- Permissions with groups/wildcards  
- Configurable YAML/JSON settings  
- Multi-language support  
- Popular integrations: Vault, PAPI, BlueMap  
- Automatic backups & logging  

---

## 📋 Commands
Commands are grouped by system. Full details in the [Wiki](../../../../Downloads/wiki-link).

### Town Commands
```
/town create <name>          - Create a new town
/town delete                 - Delete your town
/town claim                  - Claim current chunk
/town unclaim                - Unclaim chunk
/town invite <player>        - Invite player
/town kick <player>          - Kick player
/town bank [deposit|withdraw] <amount> - Manage finances
/town info [town]            - Town info
/town spawn                  - Teleport to town spawn
```

### Nation Commands
```
/nation create <name>        - Create a new nation
/nation delete               - Delete nation
/nation join <nation>        - Join nation
/nation leave                - Leave nation
/nation ally <nation>        - Ally nation
/nation enemy <nation>       - Enemy nation
```

### War Commands
```
/war declare <nation> [type] - Declare war
/war surrender               - Surrender war
/war info [nation]           - Info
/war history                 - History
```

### Mercenary Commands
```
/mercenary create <name>     - Create clan
/mercenary disband           - Disband clan
/mercenary hire <clan>       - Hire clan
```

### Chat Commands
```
/empirechat channel <type>   - Switch channels
/empirechat spy              - Toggle spy mode
```

### Admin Commands
```
/empireadmin reload          - Reload config
/empireadmin backup          - Backup data
```

---

## 🔐 Permissions
Examples:
```
empires.town.create     - Create towns
empires.nation.create   - Create nations
empires.war.declare     - Declare wars
empires.mercenary.hire  - Hire clans
empires.chat.spy.use    - Spy chat
empires.admin           - Admin features
```

---

## 📦 Installation
1. Download `EmpiresOfAlan.jar`  
2. Place it in `/plugins/`  
3. Install dependencies (Vault, optional PAPI/BlueMap)  
4. Start server to generate configs  
5. Edit `config.yml`, then restart  

---

## ⚙️ Configuration
Key configs are generated on startup:
- `config.yml` - Main settings  
- `lang/en_US.yml` - Language file  
- `permissions.yml` - Permissions  
- `data/empires.db` - SQLite database  

---

## 🔧 API Integration
### Example Event
```java
@EventHandler
public void onTownCreate(TownCreateEvent event) {
    Town town = event.getTown();
    String creator = event.getCreatorName();
    // Custom logic
}
```

### Placeholders
- `%empires_town_name%`  
- `%empires_nation_name%`  
- `%empires_resident_role%`  

---

## 🤝 Contributing
1. Fork repo  
2. Create branch  
3. Commit & push  
4. Submit PR  

---

## 📜 License
This project is licensed under the **BSD 2-Clause License**.

---

## 🙏 Acknowledgments
- PaperMC team  
- Vault, PlaceholderAPI, BlueMap devs  
- Inspired by Towny & Factions  
- Minecraft community support  

**EmpiresOfAlan – Build Your Empire Today!**

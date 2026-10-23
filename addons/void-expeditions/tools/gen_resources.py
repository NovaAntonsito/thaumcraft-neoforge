"""
Writes Void Expeditions' data and asset JSON (recipes, loot, models, blockstates, research, lang) from the
dictionaries below. The lang files are written whole: edit the texts here, not in the JSON.

Run from the project root:  python addons/void-expeditions/tools/gen_resources.py addons/void-expeditions/src/main/resources
"""
import json, os, sys

ROOT = sys.argv[1]


def w(path, obj):
    path = os.path.join(ROOT, path)
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, 'w', encoding='utf-8', newline='\n') as f:
        json.dump(obj, f, indent=2, ensure_ascii=False)
        f.write('\n')


A = 'assets/voidexpeditions/'
D = 'data/voidexpeditions/'

w(D + 'dimension/expedition.json', {"type": "thaumcraft:void", "generator": {"type": "minecraft:flat", "settings": {
    "biome": "thaumcraft:void", "layers": [{"block": "minecraft:structure_void", "height": 1}], "lakes": False,
    "features": True, "structure_overrides": []}}})

w(D + 'recipe/expedition_terminal.json', {
    "type": "thaumcraft:arcane_shaped", "research": "VOIDGATE", "vis": 250,
    "crystals": {"ordo": 2, "perditio": 2}, "pattern": ["PMP", "STS", "PSP"],
    "key": {"P": {"tag": "c:plates/void"}, "M": {"item": "thaumcraft:mirrored_glass"},
            "S": {"item": "thaumcraft:void_seed"}, "T": {"item": "thaumcraft:runed_tablet"}},
    "result": {"id": "voidexpeditions:expedition_terminal", "count": 1}})

w(D + 'recipe/void_gate_ring.json', {
    "type": "thaumcraft:arcane_shaped", "research": "VOIDGATE", "vis": 150,
    "crystals": {"ordo": 1, "perditio": 2}, "pattern": ["OPO", "PSP", "OPO"],
    "key": {"O": {"item": "minecraft:crying_obsidian"}, "P": {"tag": "c:plates/void"},
            "S": {"item": "thaumcraft:void_seed"}},
    "result": {"id": "voidexpeditions:void_gate_ring", "count": 4}})

for b in ("expedition_terminal", "void_gate_ring"):
    w(D + 'loot_table/blocks/' + b + '.json', {"type": "minecraft:block", "pools": [{
        "rolls": 1, "entries": [{"type": "minecraft:item", "name": "voidexpeditions:" + b}],
        "conditions": [{"condition": "minecraft:survives_explosion"}]}]})
w(A + 'blockstates/expedition_terminal.json', {"variants": {"": {"model": "voidexpeditions:block/expedition_terminal"}}})
w(A + 'models/item/expedition_terminal.json', {"parent": "voidexpeditions:block/expedition_terminal"})
w(A + 'blockstates/void_gate_ring.json', {"variants": {
    "formed=false": {"model": "voidexpeditions:block/void_gate_ring"},
    "formed=true": {"model": "voidexpeditions:block/void_gate_ring_formed"}}})
w(A + 'models/item/void_gate_ring.json', {"parent": "voidexpeditions:block/void_gate_ring_formed"})
w(A + 'blockstates/void_horizon.json', {"variants": {
    "axis=x": {"model": "voidexpeditions:block/void_horizon"},
    "axis=z": {"model": "voidexpeditions:block/void_horizon", "y": 90}}})

w('data/minecraft/tags/block/mineable/pickaxe.json',
  {"replace": False, "values": ["voidexpeditions:expedition_terminal", "voidexpeditions:void_gate_ring"]})

w(A + 'research/expeditions.json', {"entries": [{
    "key": "VOIDGATE", "name": "research.VOIDGATE.title", "icons": ["voidexpeditions:void_gate_ring"],
    "parents": ["VOIDEXPLORER"], "category": "VOID", "location": [14, 1], "meta": ["ROUND", "SPIKY", "HIDDEN"],
    "stages": [{"text": "research.VOIDGATE.stage.1",
                "recipes": ["voidexpeditions:void_gate_ring", "voidexpeditions:expedition_terminal"]}]}]})

w(A + 'models/block/expedition_terminal.json', {"parent": "minecraft:block/cube_bottom_top", "textures": {
    "side": "thaumcraft:blocks/obsidiantile", "top": "thaumcraft:blocks/metal_void",
    "bottom": "thaumcraft:blocks/obsidiantile"}})

GLOW = {"block_light": 15, "sky_light": 15}
SIDES = ("north", "south", "east", "west")


def cube_faces(tex, cull, glow=False):
    faces = {}
    for d in ("north", "south", "east", "west", "up", "down"):
        f = {"uv": [0, 0, 16, 16], "texture": tex}
        if cull:
            f["cullface"] = d
        if glow:
            f["neoforge_data"] = GLOW
        faces[d] = f
    return faces


w(A + 'models/block/void_gate_ring.json', {"parent": "minecraft:block/cube_all",
                                           "textures": {"all": "thaumcraft:blocks/obsidiantile"}})
w(A + 'models/block/void_gate_ring_formed.json', {
    "parent": "minecraft:block/block", "render_type": "minecraft:cutout",
    "textures": {"particle": "thaumcraft:blocks/obsidiantile", "base": "thaumcraft:blocks/obsidiantile",
                 "chevron": "voidexpeditions:block/void_gate_chevron"},
    "elements": [
        {"name": "segment", "from": [0, 0, 0], "to": [16, 16, 16], "faces": cube_faces("#base", True)},
        {"name": "chevron", "from": [-0.01, -0.01, -0.01], "to": [16.01, 16.01, 16.01], "shade": False,
         "faces": {d: {"uv": [0, 0, 16, 16], "texture": "#chevron", "cullface": d, "neoforge_data": GLOW} for d in SIDES}},
    ]})
w(A + 'models/block/void_horizon.json', {
    "parent": "minecraft:block/block", "render_type": "minecraft:translucent",
    "textures": {"particle": "minecraft:block/nether_portal", "veil": "minecraft:block/nether_portal"},
    "elements": [
        {"name": "veil", "from": [0, 0, 7], "to": [16, 16, 9], "shade": False,
         "faces": {"north": {"uv": [0, 0, 16, 16], "texture": "#veil", "neoforge_data": GLOW},
                   "south": {"uv": [0, 0, 16, 16], "texture": "#veil", "neoforge_data": GLOW}}},
    ]})
w(A + 'models/item/void_sigil.json',
  {"parent": "minecraft:item/generated", "textures": {"layer0": "thaumcraft:items/eldritch_object_2"}})
w(A + 'models/item/void_shard.json',
  {"parent": "minecraft:item/generated", "textures": {"layer0": "thaumcraft:items/nugget_void"}})

en = {
    "itemGroup.voidexpeditions": "Void Expeditions",
    "block.voidexpeditions.expedition_terminal": "Expedition Terminal",
    "block.voidexpeditions.void_gate_ring": "Void Gate Ring",
    "block.voidexpeditions.void_horizon": "Void Horizon",
    "voidexpeditions.gate.incomplete": "The ring is not whole: sixteen segments in a circle seven blocks across, empty inside.",
    "voidexpeditions.gate.no_ring": "There is no whole ring before this terminal.",
    "voidexpeditions.gate.no_terminal_slot": "The ring needs its terminal: on the ground, two blocks in front of or behind its lowest segment.",
    "item.voidexpeditions.void_sigil": "Void Sigil",
    "item.voidexpeditions.void_shard": "Void Shard",
    "voidexpeditions.difficulty.restless": "Restless",
    "voidexpeditions.difficulty.abyssal": "Abyssal",
    "voidexpeditions.difficulty.primordial": "Primordial",
    "voidexpeditions.terminal.title": "Expedition Terminal",
    "voidexpeditions.terminal.hint": "Step into a Void Gate nearby to set out.",
    "voidexpeditions.terminal.difficulty": "Difficulty: %s",
    "voidexpeditions.terminal.length": "Length: %s encounters",
    "voidexpeditions.gate.no_terminal": "The gate is silent: no terminal nearby guides it.",
    "voidexpeditions.run.start": "The void opens: %s expedition, %s encounters.",
    "voidexpeditions.run.encounter": "Encounter %s of %s",
    "voidexpeditions.run.won": "Expedition complete (%s). The void yields its treasure and %s shards.",
    "voidexpeditions.run.abandoned": "You turn back. The void forgets your gifts.",
    "voidexpeditions.run.lost": "The void claims your expedition. Its gifts fade.",
    "voidexpeditions.portal.no_sigil": "The portal needs a Void Sigil. Sneak into it to give up the expedition.",
    "voidexpeditions.perk.choose": "Choose a gift of the void",
    "voidexpeditions.perk.next": "Next: encounter %s of %s",
    "voidexpeditions.perk.gained": "Gift gained: %s",
    "voidexpeditions.perk.vitality": "Vitality", "voidexpeditions.perk.vitality.desc": "+2 hearts",
    "voidexpeditions.perk.bulwark": "Bulwark", "voidexpeditions.perk.bulwark.desc": "+3 armor",
    "voidexpeditions.perk.swiftness": "Swiftness", "voidexpeditions.perk.swiftness.desc": "+12% speed",
    "voidexpeditions.perk.might": "Might", "voidexpeditions.perk.might.desc": "+2 attack damage",
    "voidexpeditions.perk.steadfast": "Steadfast", "voidexpeditions.perk.steadfast.desc": "+25% knockback resistance",
    "voidexpeditions.perk.fortune": "Fortune", "voidexpeditions.perk.fortune.desc": "+1 luck",
    "voidexpeditions.perk.vis_thrift": "Vis Thrift", "voidexpeditions.perk.vis_thrift.desc": "Casting costs 15% less vis",
    "voidexpeditions.perk.potency": "Potency", "voidexpeditions.perk.potency.desc": "Foci are 25% stronger",
    "voidexpeditions.perk.quickcast": "Quickcast", "voidexpeditions.perk.quickcast.desc": "Foci recover 20% faster",
    "voidexpeditions.research.revealed": "The void remembers you. A way back in reveals itself...",
    "research.VOIDGATE.title": "The Void Gate",
    "research.VOIDGATE.stage.1": "The labyrinths of the void were never meant to be visited only once. With the pages of the journal gathered, you understand how the rifts were opened, and how to open one of your own.<BR>A Void Gate leads into a chain of labyrinths; its Expedition Terminal sets how deep and how dangerous. In every labyrinth a Void Sigil waits in the key room: carry it to the central portal to go on, and the void offers you one of three gifts. The gifts last only while the expedition does.<BR>Die, and the expedition is lost with its gifts, though you keep what you carry. Sneak into a portal without a sigil to give it up. Reach the end, and the void rewards you with treasure and Void Shards.",
}

es = {
    "itemGroup.voidexpeditions": "Expediciones del Vacío",
    "block.voidexpeditions.expedition_terminal": "Terminal de expedición",
    "block.voidexpeditions.void_gate_ring": "Segmento de la Puerta del Vacío",
    "block.voidexpeditions.void_horizon": "Horizonte del vacío",
    "voidexpeditions.gate.incomplete": "El anillo no está completo: dieciséis segmentos en un círculo de siete bloques, vacío por dentro.",
    "voidexpeditions.gate.no_ring": "No hay un anillo completo frente a esta terminal.",
    "voidexpeditions.gate.no_terminal_slot": "El anillo necesita su terminal: en el suelo, a dos bloques delante o detrás de su segmento más bajo.",
    "item.voidexpeditions.void_sigil": "Sello del Vacío",
    "item.voidexpeditions.void_shard": "Fragmento del Vacío",
    "voidexpeditions.difficulty.restless": "Inquieto",
    "voidexpeditions.difficulty.abyssal": "Abismal",
    "voidexpeditions.difficulty.primordial": "Primordial",
    "voidexpeditions.terminal.title": "Terminal de expedición",
    "voidexpeditions.terminal.hint": "Entrá en una Puerta del Vacío cercana para partir.",
    "voidexpeditions.terminal.difficulty": "Dificultad: %s",
    "voidexpeditions.terminal.length": "Largo: %s encuentros",
    "voidexpeditions.gate.no_terminal": "La puerta está muda: no hay una terminal cerca que la guíe.",
    "voidexpeditions.run.start": "El vacío se abre: expedición %s, %s encuentros.",
    "voidexpeditions.run.encounter": "Encuentro %s de %s",
    "voidexpeditions.run.won": "Expedición completa (%s). El vacío entrega su tesoro y %s fragmentos.",
    "voidexpeditions.run.abandoned": "Das la vuelta. El vacío olvida sus dones.",
    "voidexpeditions.run.lost": "El vacío reclama tu expedición. Sus dones se desvanecen.",
    "voidexpeditions.portal.no_sigil": "El portal necesita un Sello del Vacío. Agachate al entrar para abandonar la expedición.",
    "voidexpeditions.perk.choose": "Elegí un don del vacío",
    "voidexpeditions.perk.next": "Siguiente: encuentro %s de %s",
    "voidexpeditions.perk.gained": "Don obtenido: %s",
    "voidexpeditions.perk.vitality": "Vitalidad", "voidexpeditions.perk.vitality.desc": "+2 corazones",
    "voidexpeditions.perk.bulwark": "Baluarte", "voidexpeditions.perk.bulwark.desc": "+3 de armadura",
    "voidexpeditions.perk.swiftness": "Presteza", "voidexpeditions.perk.swiftness.desc": "+12% de velocidad",
    "voidexpeditions.perk.might": "Fuerza", "voidexpeditions.perk.might.desc": "+2 de daño de ataque",
    "voidexpeditions.perk.steadfast": "Firmeza", "voidexpeditions.perk.steadfast.desc": "+25% de resistencia al empuje",
    "voidexpeditions.perk.fortune": "Fortuna", "voidexpeditions.perk.fortune.desc": "+1 de suerte",
    "voidexpeditions.perk.vis_thrift": "Ahorro de vis", "voidexpeditions.perk.vis_thrift.desc": "Lanzar cuesta 15% menos vis",
    "voidexpeditions.perk.potency": "Potencia", "voidexpeditions.perk.potency.desc": "Los focos son 25% más fuertes",
    "voidexpeditions.perk.quickcast": "Lanzamiento rápido", "voidexpeditions.perk.quickcast.desc": "Los focos se recargan 20% más rápido",
    "voidexpeditions.research.revealed": "El vacío te recuerda. Un camino de vuelta se revela...",
    "research.VOIDGATE.title": "La Puerta del Vacío",
    "research.VOIDGATE.stage.1": "Los laberintos del vacío nunca fueron hechos para visitarse una sola vez. Con las páginas del diario reunidas, entendés cómo se abrían las grietas, y cómo abrir una propia.<BR>Una Puerta del Vacío lleva a una cadena de laberintos; su Terminal de expedición decide qué tan hondo y qué tan peligroso. En cada laberinto espera un Sello del Vacío en la sala de la llave: llevalo al portal del centro para seguir, y el vacío te ofrece uno de tres dones. Los dones duran solo lo que dura la expedición.<BR>Si morís, la expedición se pierde con sus dones, aunque conservás lo que llevás encima. Agachate al entrar a un portal sin sello para abandonarla. Llegá al final y el vacío te recompensa con tesoros y Fragmentos del Vacío.",
}

en.update({
    "voidexpeditions.perk.vis_siphon": "Vis Siphon", "voidexpeditions.perk.vis_siphon.desc": "The aura around you refills toward its base",
    "voidexpeditions.perk.flux_ward": "Flux Ward", "voidexpeditions.perk.flux_ward.desc": "The flux around you is cleansed",
    "voidexpeditions.perk.warp_ward": "Warp Ward", "voidexpeditions.perk.warp_ward.desc": "No warp events during the expedition",
    "voidexpeditions.perk.clear_mind": "Clear Mind", "voidexpeditions.perk.clear_mind.desc": "Each portal washes away your temporary warp",
    "voidexpeditions.perk.night_eye": "Night Eye", "voidexpeditions.perk.night_eye.desc": "Night vision",
    "voidexpeditions.perk.sigil_sense": "Sigil Sense", "voidexpeditions.perk.sigil_sense.desc": "Void Sigils glow through walls",
    "voidexpeditions.perk.void_compass": "Void Compass", "voidexpeditions.perk.void_compass.desc": "Shows the way to the labyrinth's portal",
    "voidexpeditions.perk.void_compass.bar": "Portal %s %s blocks",
    "voidexpeditions.perk.blood_pact": "Blood Pact", "voidexpeditions.perk.blood_pact.desc": "Foci +50% stronger, but -2 hearts",
    "voidexpeditions.perk.glass_cannon": "Glass Cannon", "voidexpeditions.perk.glass_cannon.desc": "+4 attack damage, but -4 armor",
    "voidexpeditions.perk.frenzy": "Frenzy", "voidexpeditions.perk.frenzy.desc": "Foci recover 35% faster, but cost 20% more vis",
    "voidexpeditions.portal.boss": "The portal waits for the labyrinth's guardian to fall. Sneak into it to give up the expedition.",
    "voidexpeditions.run.boss_slain": "The guardian falls. The last portal opens.",
    "voidexpeditions.run.extra_life": "The void refuses to take you yet. Your extra life is spent.",
    "voidexpeditions.run.daily_limit": "The void is restless. No more than %s expeditions a day.",
    "voidexpeditions.terminal.runs_left": "Expeditions left today: %s",
    "voidexpeditions.shop.title": "Shop · Void Shards: %s",
    "voidexpeditions.shop.priced": "%s · %s shards",
    "voidexpeditions.shop.owned": "%s ✔",
    "voidexpeditions.shop.extra_life": "Extra life",
    "voidexpeditions.shop.extra_life.desc": "Next expedition: the first death does not end it",
    "voidexpeditions.shop.start_perk.desc": "Start the next expedition with this gift: %s",
    "voidexpeditions.shop.focus": "Void focus",
    "voidexpeditions.shop.focus.desc": "An advanced focus of random powers, yours to keep. One per expedition.",
    "voidexpeditions.shop.bought": "Bought.",
    "voidexpeditions.shop.too_poor": "Not enough Void Shards.",
    "voidexpeditions.shop.during_run": "The shop is closed during an expedition.",
    "voidexpeditions.shop.focus_once": "Only one void focus per expedition.",
    "voidexpeditions.focus.name": "Void Focus",
    "research.VOIDGATE.stage.1": "The labyrinths of the void were never meant to be visited only once. With the pages of the journal gathered, you understand how the rifts were opened, and how to open one of your own.<BR>A Void Gate leads into a chain of labyrinths; its Expedition Terminal sets how deep and how dangerous. In every labyrinth a Void Sigil waits in the key room: carry it to the central portal to go on, and the void offers you one of three gifts. The gifts last only while the expedition does. In the last labyrinth there is no sigil to bring: its guardian must fall.<BR>Die, and the expedition is lost with its gifts, though you keep what you carry. Sneak into a portal that will not open to give it up. Reach the end, and the void rewards you with treasure and Void Shards, which the terminal takes for an extra life, a gift to start with or a focus of the void. The void tires quickly: no more than two expeditions a day.",
})
es.update({
    "voidexpeditions.perk.vis_siphon": "Sifón de vis", "voidexpeditions.perk.vis_siphon.desc": "El aura a tu alrededor se recarga hacia su base",
    "voidexpeditions.perk.flux_ward": "Guarda de flux", "voidexpeditions.perk.flux_ward.desc": "El flux a tu alrededor se limpia",
    "voidexpeditions.perk.warp_ward": "Guarda del warp", "voidexpeditions.perk.warp_ward.desc": "Sin eventos de warp durante la expedición",
    "voidexpeditions.perk.clear_mind": "Mente clara", "voidexpeditions.perk.clear_mind.desc": "Cada portal borra tu warp temporal",
    "voidexpeditions.perk.night_eye": "Ojo nocturno", "voidexpeditions.perk.night_eye.desc": "Visión nocturna",
    "voidexpeditions.perk.sigil_sense": "Sentir el sello", "voidexpeditions.perk.sigil_sense.desc": "Los Sellos del Vacío brillan a través de las paredes",
    "voidexpeditions.perk.void_compass": "Brújula del vacío", "voidexpeditions.perk.void_compass.desc": "Muestra el camino al portal del laberinto",
    "voidexpeditions.perk.void_compass.bar": "Portal %s %s bloques",
    "voidexpeditions.perk.blood_pact": "Pacto de sangre", "voidexpeditions.perk.blood_pact.desc": "Focos 50% más fuertes, pero -2 corazones",
    "voidexpeditions.perk.glass_cannon": "Cañón de cristal", "voidexpeditions.perk.glass_cannon.desc": "+4 de daño de ataque, pero -4 de armadura",
    "voidexpeditions.perk.frenzy": "Frenesí", "voidexpeditions.perk.frenzy.desc": "Los focos se recargan 35% más rápido, pero cuestan 20% más vis",
    "voidexpeditions.portal.boss": "El portal espera la caída del guardián del laberinto. Agachate al entrar para abandonar la expedición.",
    "voidexpeditions.run.boss_slain": "El guardián cae. El último portal se abre.",
    "voidexpeditions.run.extra_life": "El vacío todavía no te reclama. Gastaste tu vida extra.",
    "voidexpeditions.run.daily_limit": "El vacío está inquieto. No más de %s expediciones por día.",
    "voidexpeditions.terminal.runs_left": "Expediciones que quedan hoy: %s",
    "voidexpeditions.shop.title": "Tienda · Fragmentos del Vacío: %s",
    "voidexpeditions.shop.priced": "%s · %s fragmentos",
    "voidexpeditions.shop.owned": "%s ✔",
    "voidexpeditions.shop.extra_life": "Vida extra",
    "voidexpeditions.shop.extra_life.desc": "Próxima expedición: la primera muerte no la termina",
    "voidexpeditions.shop.start_perk.desc": "Empezás la próxima expedición con este don: %s",
    "voidexpeditions.shop.focus": "Foco del vacío",
    "voidexpeditions.shop.focus.desc": "Un foco avanzado de poderes al azar, para quedártelo. Uno por expedición.",
    "voidexpeditions.shop.bought": "Comprado.",
    "voidexpeditions.shop.too_poor": "No te alcanzan los Fragmentos del Vacío.",
    "voidexpeditions.shop.during_run": "La tienda está cerrada durante una expedición.",
    "voidexpeditions.shop.focus_once": "Solo un foco del vacío por expedición.",
    "voidexpeditions.focus.name": "Foco del Vacío",
    "research.VOIDGATE.stage.1": "Los laberintos del vacío nunca fueron hechos para visitarse una sola vez. Con las páginas del diario reunidas, entendés cómo se abrían las grietas, y cómo abrir una propia.<BR>Una Puerta del Vacío lleva a una cadena de laberintos; su Terminal de expedición decide qué tan hondo y qué tan peligroso. En cada laberinto espera un Sello del Vacío en la sala de la llave: llevalo al portal del centro para seguir, y el vacío te ofrece uno de tres dones. Los dones duran solo lo que dura la expedición. En el último laberinto no hay sello que llevar: su guardián tiene que caer.<BR>Si morís, la expedición se pierde con sus dones, aunque conservás lo que llevás encima. Agachate al entrar a un portal que no se abre para abandonarla. Llegá al final y el vacío te recompensa con tesoros y Fragmentos del Vacío, que la terminal acepta a cambio de una vida extra, un don para empezar o un foco del vacío. El vacío se cansa rápido: no más de dos expediciones por día.",
})

en.update({
    "voidexpeditions.perk.category.player": "Gift of the body",
    "voidexpeditions.perk.category.caster": "Gift of the caster",
    "voidexpeditions.perk.category.aura": "Gift of the aura",
    "voidexpeditions.perk.category.warp": "Gift of the mind",
    "voidexpeditions.perk.category.utility": "Gift of insight",
    "voidexpeditions.perk.category.cursed": "Cursed gift",
    "voidexpeditions.difficulty.restless.desc": "The guardians are few and the void is gentle.",
    "voidexpeditions.difficulty.abyssal.desc": "More guardians, a champion among them, and richer chests.",
    "voidexpeditions.difficulty.primordial.desc": "Many guardians, several champions, the richest chests and the most shards.",
    "voidexpeditions.terminal.length.desc": "How many labyrinths the expedition goes through. The last one has a guardian to defeat.",
    "voidexpeditions.terminal.hint": "Click to change, right click to go back. Step into a Void Gate nearby to set out.",
    "voidexpeditions.hud.encounter": "Encounter %s/%s · %s",
    "voidexpeditions.hud.goal.sigil": "◆ Void Sigil",
    "voidexpeditions.hud.goal.boss": "◆ The labyrinth's guardian",
    "voidexpeditions.hud.extra_life": "✚ Extra life",
})
es.update({
    "voidexpeditions.perk.category.player": "Don del cuerpo",
    "voidexpeditions.perk.category.caster": "Don del taumaturgo",
    "voidexpeditions.perk.category.aura": "Don del aura",
    "voidexpeditions.perk.category.warp": "Don de la mente",
    "voidexpeditions.perk.category.utility": "Don de la percepción",
    "voidexpeditions.perk.category.cursed": "Don maldito",
    "voidexpeditions.difficulty.restless.desc": "Pocos guardianes; el vacío es benévolo.",
    "voidexpeditions.difficulty.abyssal.desc": "Más guardianes, algún campeón entre ellos, y cofres más ricos.",
    "voidexpeditions.difficulty.primordial.desc": "Muchos guardianes, varios campeones, los cofres más ricos y más fragmentos.",
    "voidexpeditions.terminal.length.desc": "Cuántos laberintos atraviesa la expedición. El último tiene un guardián que derrotar.",
    "voidexpeditions.terminal.hint": "Clic para cambiar, clic derecho para volver. Entrá en una Puerta del Vacío cercana para partir.",
    "voidexpeditions.hud.encounter": "Encuentro %s/%s · %s",
    "voidexpeditions.hud.goal.sigil": "◆ Sello del Vacío",
    "voidexpeditions.hud.goal.boss": "◆ El guardián del laberinto",
    "voidexpeditions.hud.extra_life": "✚ Vida extra",
})

en.update({
    "voidexpeditions.terminal.hint": "Click to change, right click to go back. Step into the gate's horizon to set out.",
    "voidexpeditions.gate.no_terminal": "The horizon wavers: its terminal is gone.",
    "research.VOIDGATE.stage.1": "The labyrinths of the void were never meant to be visited only once. With the pages of the journal gathered, you understand how the rifts were opened, and how to open one of your own.<BR>Raise sixteen Void Gate Rings standing upright, a circle seven blocks across with nothing inside, and set an Expedition Terminal on the ground two blocks in front of or behind its lowest segment. Touch the ring or the terminal with a caster: the ring lights up and the void horizon fills it. Take any part away and the horizon collapses.<BR>The terminal sets how deep and how dangerous the expedition is. Step into the horizon to set out. In every labyrinth a Void Sigil waits in the key room: carry it to the central portal to go on, and the void offers you one of three gifts. The gifts last only while the expedition does. In the last labyrinth there is no sigil to bring: its guardian must fall.<BR>Die, and the expedition is lost with its gifts, though you keep what you carry. Sneak into a portal that will not open to give it up. Reach the end, and the void rewards you with treasure and Void Shards, which the terminal takes for an extra life, a gift to start with or a focus of the void. The void tires quickly: no more than two expeditions a day.",
})
es.update({
    "voidexpeditions.terminal.hint": "Clic para cambiar, clic derecho para volver. Entrá en el horizonte de la puerta para partir.",
    "voidexpeditions.gate.no_terminal": "El horizonte vacila: su terminal no está.",
    "research.VOIDGATE.stage.1": "Los laberintos del vacío nunca fueron hechos para visitarse una sola vez. Con las páginas del diario reunidas, entendés cómo se abrían las grietas, y cómo abrir una propia.<BR>Levantá dieciséis Segmentos de la Puerta del Vacío, de pie, en un círculo de siete bloques de ancho y vacío por dentro, y poné una Terminal de expedición en el suelo, a dos bloques delante o detrás de su segmento más bajo. Tocá el anillo o la terminal con un caster: el anillo se enciende y el horizonte del vacío lo llena. Si sacás cualquier parte, el horizonte colapsa.<BR>La terminal decide qué tan hondo y qué tan peligroso. Entrá al horizonte para partir. En cada laberinto espera un Sello del Vacío en la sala de la llave: llevalo al portal del centro para seguir, y el vacío te ofrece uno de tres dones. Los dones duran solo lo que dura la expedición. En el último laberinto no hay sello que llevar: su guardián tiene que caer.<BR>Si morís, la expedición se pierde con sus dones, aunque conservás lo que llevás encima. Agachate al entrar a un portal que no se abre para abandonarla. Llegá al final y el vacío te recompensa con tesoros y Fragmentos del Vacío, que la terminal acepta a cambio de una vida extra, un don para empezar o un foco del vacío. El vacío se cansa rápido: no más de dos expediciones por día.",
})

en.update({
    "voidexpeditions.ponder.tag.void_expeditions": "Void Expeditions",
    "voidexpeditions.ponder.tag.void_expeditions.description": "The void gate and the expeditions through it",
    "voidexpeditions.ponder.void_gate.header": "Raising the Void Gate",
    "voidexpeditions.ponder.void_gate.text_1": "Sixteen Void Gate Rings stand upright in a circle seven blocks across. Nothing may be inside it",
    "voidexpeditions.ponder.void_gate.text_2": "Its Expedition Terminal goes on the ground, two blocks in front of or behind the lowest segment",
    "voidexpeditions.ponder.void_gate.text_3": "Touch the terminal or any segment with a Caster: the ring lights up and the void horizon fills it",
    "voidexpeditions.ponder.void_gate.text_4": "Right-click the terminal to choose the difficulty and length of the expedition, and to spend Void Shards in its shop",
    "voidexpeditions.ponder.void_gate.text_5": "Take any part away, a segment or the terminal, and the horizon collapses",
    "voidexpeditions.ponder.void_expedition.header": "Setting out on an Expedition",
    "voidexpeditions.ponder.void_expedition.text_1": "Step into the horizon: the void takes you through a chain of labyrinths, as many as the terminal says",
    "voidexpeditions.ponder.void_expedition.text_2": "In each labyrinth a Void Sigil waits in the key room. Carry it to the portal in the middle to go on",
    "voidexpeditions.ponder.void_expedition.text_3": "The portal offers three gifts of the void: pick one. Gifts stack, and last only while the expedition does",
    "voidexpeditions.ponder.void_expedition.text_4": "The last labyrinth has no sigil: open its boss room with the runed tablet and defeat the guardian",
    "voidexpeditions.ponder.void_expedition.text_5": "Come back with treasure and Void Shards. Dying loses the expedition and its gifts, but not what you carry",
})
es.update({
    "voidexpeditions.ponder.tag.void_expeditions": "Expediciones del Vacío",
    "voidexpeditions.ponder.tag.void_expeditions.description": "La puerta del vacío y las expediciones a través de ella",
    "voidexpeditions.ponder.void_gate.header": "Levantar la Puerta del Vacío",
    "voidexpeditions.ponder.void_gate.text_1": "Dieciséis Segmentos de la Puerta del Vacío van de pie, en un círculo de siete bloques de ancho. Adentro no puede haber nada",
    "voidexpeditions.ponder.void_gate.text_2": "Su Terminal de expedición va en el suelo, a dos bloques delante o detrás del segmento más bajo",
    "voidexpeditions.ponder.void_gate.text_3": "Tocá la terminal o cualquier segmento con un Caster: el anillo se enciende y el horizonte del vacío lo llena",
    "voidexpeditions.ponder.void_gate.text_4": "Clic derecho en la terminal para elegir la dificultad y el largo de la expedición, y para gastar Fragmentos del Vacío en su tienda",
    "voidexpeditions.ponder.void_gate.text_5": "Si sacás cualquier parte, un segmento o la terminal, el horizonte colapsa",
    "voidexpeditions.ponder.void_expedition.header": "Partir en una expedición",
    "voidexpeditions.ponder.void_expedition.text_1": "Entrá al horizonte: el vacío te lleva por una cadena de laberintos, tantos como diga la terminal",
    "voidexpeditions.ponder.void_expedition.text_2": "En cada laberinto espera un Sello del Vacío en la sala de la llave. Llevalo al portal del centro para seguir",
    "voidexpeditions.ponder.void_expedition.text_3": "El portal ofrece tres dones del vacío: elegí uno. Los dones se acumulan y duran solo lo que dura la expedición",
    "voidexpeditions.ponder.void_expedition.text_4": "El último laberinto no tiene sello: abrí la sala del jefe con la tablilla rúnica y derrotá al guardián",
    "voidexpeditions.ponder.void_expedition.text_5": "Volvé con tesoros y Fragmentos del Vacío. Si morís perdés la expedición y sus dones, pero no lo que llevás encima",
})

w(A + 'lang/en_us.json', en)
w(A + 'lang/es_es.json', es)
print("ok")

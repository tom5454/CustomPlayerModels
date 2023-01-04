(function() {
	var import_action, export_action, add_more_parts;
	Plugin.register('cpm_plugin', {
		name: 'Customizable Player Models Plugin',
		author: 'tom5454',
		description: 'Customizable Player Models Project (.cpmproject) support for Blockbench.',
		tags: ["Minecraft: Java Edition", "Modded"],
		icon: 'icon-player',
		version: '0.0.5',
		variant: 'both',
		onload() {
			//================================
			//== Generated part do not edit ==
			//================================
			var VANILLA_VALUES = {
				head: {
					pos: { x: 0.0, y: 0.0, z: 0.0 },
					offset: { x: -4.0, y: -8.0, z: -4.0 },
					size: { x: 8.0, y: 8.0, z: 8.0 },
					uv: { u: 0, v: 0 },
					layer: "hat",
					layerUV: { u: 32, v: 0 },
				},
				body: {
					pos: { x: 0.0, y: 0.0, z: 0.0 },
					offset: { x: -4.0, y: 0.0, z: -2.0 },
					size: { x: 8.0, y: 12.0, z: 4.0 },
					uv: { u: 16, v: 16 },
					layer: "jacket",
					layerUV: { u: 16, v: 32 },
				},
				left_arm: {
					pos: { x: 5.0, y: 2.0, z: 0.0 },
					offset: { x: -1.0, y: -2.0, z: -2.0 },
					size: { x: 4.0, y: 12.0, z: 4.0 },
					uv: { u: 32, v: 48 },
					layer: "left_sleeve",
					layerUV: { u: 48, v: 48 },
				},
				right_arm: {
					pos: { x: -5.0, y: 2.0, z: 0.0 },
					offset: { x: -3.0, y: -2.0, z: -2.0 },
					size: { x: 4.0, y: 12.0, z: 4.0 },
					uv: { u: 40, v: 16 },
					layer: "right_sleeve",
					layerUV: { u: 40, v: 32 },
				},
				left_leg: {
					pos: { x: 1.9, y: 12.0, z: 0.0 },
					offset: { x: -2.0, y: 0.0, z: -2.0 },
					size: { x: 4.0, y: 12.0, z: 4.0 },
					uv: { u: 16, v: 48 },
					layer: "left_pants_leg",
					layerUV: { u: 0, v: 48 },
				},
				right_leg: {
					pos: { x: -1.9, y: 12.0, z: 0.0 },
					offset: { x: -2.0, y: 0.0, z: -2.0 },
					size: { x: 4.0, y: 12.0, z: 4.0 },
					uv: { u: 0, v: 16 },
					layer: "right_pants_leg",
					layerUV: { u: 0, v: 32 },
				},
				cape: {
					pos: { x: 0.0, y: 0.0, z: 2.0 },
					rotation: { x: -6.000000166965211, y: 180.00000500895632, z: 0.0 },
					offset: { x: -5.0, y: 0.0, z: -1.0 },
					size: { x: 10.0, y: 16.0, z: 1.0 },
					uv: { u: 0, v: 0 },
					custom: true,
					mcscale: 0.0,
					mirror: false,
				},
				elytra_left: {
					pos: { x: 5.0, y: 0.0, z: 2.0 },
					rotation: { x: 15.000000417413029, y: 0.0, z: -15.000000417413029 },
					offset: { x: -10.0, y: 0.0, z: 0.0 },
					size: { x: 10.0, y: 20.0, z: 2.0 },
					uv: { u: 22, v: 0 },
					custom: true,
					mcscale: 1.0,
					mirror: false,
				},
				elytra_right: {
					pos: { x: -5.0, y: 0.0, z: 2.0 },
					rotation: { x: 15.000000417413029, y: 0.0, z: 15.000000417413029 },
					offset: { x: 0.0, y: 0.0, z: 0.0 },
					size: { x: 10.0, y: 20.0, z: 2.0 },
					uv: { u: 22, v: 0 },
					custom: true,
					mcscale: 1.0,
					mirror: true,
				},
				armor_helmet: {
					pos: { x: 0.0, y: 0.0, z: 0.0 },
					offset: { x: -4.0, y: -8.0, z: -4.0 },
					size: { x: 8.0, y: 8.0, z: 8.0 },
					uv: { u: 0, v: 0 },
					custom: true,
					mcscale: 1.0,
					mirror: false,
					copyFrom: "head",
				},
				armor_body: {
					pos: { x: 0.0, y: 0.0, z: 0.0 },
					offset: { x: -4.0, y: 0.0, z: -2.0 },
					size: { x: 8.0, y: 12.0, z: 4.0 },
					uv: { u: 16, v: 16 },
					custom: true,
					mcscale: 1.0,
					mirror: false,
					copyFrom: "body",
				},
				armor_left_arm: {
					pos: { x: 5.0, y: 2.0, z: 0.0 },
					offset: { x: -1.0, y: -2.0, z: -2.0 },
					size: { x: 4.0, y: 12.0, z: 4.0 },
					uv: { u: 40, v: 16 },
					custom: true,
					mcscale: 1.0,
					mirror: true,
					copyFrom: "left_arm",
				},
				armor_right_arm: {
					pos: { x: -5.0, y: 2.0, z: 0.0 },
					offset: { x: -3.0, y: -2.0, z: -2.0 },
					size: { x: 4.0, y: 12.0, z: 4.0 },
					uv: { u: 40, v: 16 },
					custom: true,
					mcscale: 1.0,
					mirror: false,
					copyFrom: "right_arm",
				},
				armor_leggings_body: {
					pos: { x: 0.0, y: 0.0, z: 0.0 },
					offset: { x: -4.0, y: 0.0, z: -2.0 },
					size: { x: 8.0, y: 12.0, z: 4.0 },
					uv: { u: 16, v: 16 },
					custom: true,
					mcscale: 0.5,
					mirror: false,
					copyFrom: "body",
				},
				armor_left_leg: {
					pos: { x: 1.9, y: 12.0, z: 0.0 },
					offset: { x: -2.0, y: 0.0, z: -2.0 },
					size: { x: 4.0, y: 12.0, z: 4.0 },
					uv: { u: 0, v: 16 },
					custom: true,
					mcscale: 0.5,
					mirror: true,
					copyFrom: "left_leg",
				},
				armor_right_leg: {
					pos: { x: -1.9, y: 12.0, z: 0.0 },
					offset: { x: -2.0, y: 0.0, z: -2.0 },
					size: { x: 4.0, y: 12.0, z: 4.0 },
					uv: { u: 0, v: 16 },
					custom: true,
					mcscale: 0.5,
					mirror: false,
					copyFrom: "right_leg",
				},
				armor_left_foot: {
					pos: { x: 1.9, y: 12.0, z: 0.0 },
					offset: { x: -2.0, y: 0.0, z: -2.0 },
					size: { x: 4.0, y: 12.0, z: 4.0 },
					uv: { u: 0, v: 16 },
					custom: true,
					mcscale: 1.0,
					mirror: true,
					copyFrom: "left_leg",
				},
				armor_right_foot: {
					pos: { x: -1.9, y: 12.0, z: 0.0 },
					offset: { x: -2.0, y: 0.0, z: -2.0 },
					size: { x: 4.0, y: 12.0, z: 4.0 },
					uv: { u: 0, v: 16 },
					custom: true,
					mcscale: 1.0,
					mirror: false,
					copyFrom: "right_leg",
				},
			};
			var ROOT_GROUPS = {
				cape: {
					parts: {
						cape: "cape",
					},
					display: "Cape",
					icon: "icon-player",
				},
				elytra: {
					parts: {
						elytra_left: "elytra",
						elytra_right: "elytra",
					},
					display: "Elytra",
					icon: "fa-feather-alt",
				},
				armor: {
					parts: {
						armor_helmet: "armor1",
						armor_body: "armor1",
						armor_left_arm: "armor1",
						armor_right_arm: "armor1",
						armor_leggings_body: "armor2",
						armor_left_leg: "armor2",
						armor_right_leg: "armor2",
						armor_left_foot: "armor1",
						armor_right_foot: "armor1",
					},
					display: "Armor",
					icon: "icon-armor_stand_small",
				},
			};
			var TEXTURES = {
				elytra: {
					src: "iVBORw0KGgoAAAANSUhEUgAAAEAAAAAgCAYAAACinX6EAAABCklEQVRo3u2YsQoCMRBE820WFiKIjVyhWAgKgliJdmcpNoKljZ8aGWFlCIGDnCK72YHhNOBx82Yj4UL4o4bTRRS3t2fWwboEQKhdAmIwbiJPhqxVBSAHoYop6QJgfhK6AJifAv4PSCFM5tu3qwEgn0ez1Sc8vNyf7UJAWAaA72KEb9YH+wA4MBtrAACbBSBB+crNw5vTVQeA4+URSyFI0xycAag4IpcCSEOn7e/auw4ApQ+Zax1rCK8KQOkEpG2nxnrpvVVMgATnxlOb3gIMgUHw+JveAgIg1z7uKTY9AQiI38tV5Zuivg8pQbl1VQD6nthy7avaAt84suaar+ZlqcvlcrlcLpfrx3oB++zXhEHDmtAAAAAASUVORK5CYII=",
				},
				armor2: {
					src: "iVBORw0KGgoAAAANSUhEUgAAAEAAAAAgCAYAAACinX6EAAABG0lEQVRo3u1WQQrCQBDrgyr4Dr/i50Q8exAf4FFvgme9eBY1hdQwrNVTobMJLN1hVjDZzOw0jWEYhmEYhmEYhmEY1WN+2D6xZvt1961SAGB5v3QiVCcASIN8ez7WK4Cuamqedc/a1z7wLZdCIJCB3RfXU08Q+1JM8tinKRGQoAAkiB6AxZtmzFvHnudTOEBvGASHHKCCpBFAb1gtrpYn4ShIqhKgAIi1BEqCME5TAiQU5wB1gApEwdL1ABJUQWKTjD1i6JmMT6p+NV/KjSJw6b3/NRvEMxydNQY2j1vfX2JeHaXQ30yixLRkKBBI0CV0kzrm3/OTEKA0SJGgktFnVAlqk43nJ9Fj2t3q8+exfy8lXMprrHNHPD+GA1545SXCqYlFUQAAAABJRU5ErkJggg==",
				},
				armor1: {
					src: "iVBORw0KGgoAAAANSUhEUgAAAEAAAAAgCAYAAACinX6EAAACXklEQVRo3uVZS0rEQBCtA406KgiiJ/Aq3sidNxARwYWCP1AEEcTvSlBxNg6CCH6QyAs8eRadzszCpkkHClLpykzX6/rHrOXqn+1UEwfrNeGexGfW9QvKPlafNW18D2tafr2vCWvFANC7u66Wnm9+qRgAYOY4dSgNEEjgiwSA5g8qIgZASSjrAVj9GpQTA6g0fR/KwyqKyQKxVNhZBb2ybUDE1mOy2aQ5ze80d24S63xGQjygMnQNEn4L71BRrSNUNosYEkpzuIef62kyGKriXIesf58xggAocAym2QCgGwuZMmVZEOm73uQVBK4BDK0hdD27GBCT5ebbAPV+HqohikqjBEFTaGeyyDiu1qn0Wczlo3suMSuZJWkfkAsAuBhH/v0PtUDKAQBNq0kA8CVsDgAoZdtLxHx3XPlYrxHrYZIAxFKXpS3I+yfLaO0NvLzfOC4+U5+nPC7yfp18EgC0l9DmiRvQZorrKq8b1maKpTIVVh7+v/L+8qcZ03UG7WQxwm+AClMZHaF5eZ6270SpAKtIBRj3828PNn2xb9Pnu7bwMTDwc0+XNb9YDS3ZzFInRmqi2jrrFHkUee0WfedJfubq0GZvj/H/1j/dtsmjTevtrcEibepky5LFAG12tG0OkW+OPCAh+WRpZdSoGnrmByh+eKIWEJL3p5+0XdaJkA9iGrX9gEP50IRIJz9NQYpge/9P+uGFJqhRmTw33HTCTbnaf0XyJu/l/ekn/fCCjTDohNKI9vLk20ZalPHzw3GKqWRVHk/YFxreZ3WdfGyT2ZWsow4qQmmnKRV1YaT1A1h3ZPaDihg7AAAAAElFTkSuQmCC",
				},
				cape: {
					src: "iVBORw0KGgoAAAANSUhEUgAAAEAAAAAgCAYAAACinX6EAAAABmJLR0QA/wD/AP+gvaeTAAAACXBIWXMAAAsTAAALEwEAmpwYAAAAB3RJTUUH5QcNCikqWH33twAAAHJJREFUaN7t11EHgFAYg+Et5///5XUVkVNSJHuf+2jzGcdJJCnWuegW29YfLPtseR784WffFPDfv3/BmIVPYwFWnzE7/ZYyDhvg5guYbYDZgMINUOMFbGFTdA1sQPsGLFcbkKYCGo3mhxAbAAAAAABApxUqvxYySSKo5wAAAABJRU5ErkJggg==",
				},
			};
			//========================
			//== Generated part end ==
			//========================
			for (var gr of Object.keys(ROOT_GROUPS)) {
				let g = ROOT_GROUPS[gr];
				for (var part of Object.keys(g.parts)) {
					VANILLA_VALUES[part].texture = g.parts[part];
				}
			}
			var NAME_MAP = {
				Head: "head",
				Body: "body",
				RightArm: "right_arm",
				LeftArm: "left_arm",
				RightLeg: "right_leg",
				LeftLeg: "left_leg"
			};
			var KNOWN_TEXTURES = {
				skin: 1,
				cape: 1,
				elytra: 1,
				armor1: 2,
				armor2: 2,
				desc_icon: 1
			};

			var errorDialog = new Dialog({ id: "cpm_error", title: "Error in CPM Plugin", lines: ["?"], confirmEnabled: false });
			var warnDialog = new Dialog({ id: "cpm_warn", title: "Warning", lines: ["?"] });
			
			var CPM_DT_GROUP = "CPM_data_DO_NOT_EDIT";

			function ceil(val) {
				return Math.ceil(val - 0.01);
			}

			function floor(val) {
				return Math.floor(val + 0.01);
			}
			
			function findRoot(cube) {
				let i = 0;
				while ((i++) < 100 && cube.parent != "root") {
					cube = cube.parent;
				}
				return cube;
			}
			
			function base64Encode(input) {
				const buf = Buffer.from(input, 'utf-8');
  				return buf.toString('base64');
			}
			
			const kBase64Digits = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=';
			
			function base64Decode(input) {
				for (let n = 0; n < input.length; n++) {
    				if (!kBase64Digits.includes(input[n]))
      					throw lazyDOMException('Invalid character', 'InvalidCharacterError');
  				}
				return Buffer.from(input, 'base64').toString('utf-8');
			}
			
			function loadResizedTexture(texName, texToUse) {
				var img = new Image();
				img.src = 'data:image/png;base64,' + TEXTURES[texName].src;
				var canvas = document.createElement('canvas');
				canvas.width = Project.texture_width;
				canvas.height = Project.texture_height;

				var ctx = canvas.getContext('2d');

				img.addEventListener('load', function() {
					ctx.drawImage(this, 0, 0);
					texToUse.fromDataURL(canvas.toDataURL());

					loadTextureDraggable()
					Canvas.updateAllBones()
					Canvas.updateVisibility()
					setProjectTitle()
					updateSelection()
				}, false);
			}
			
			function findElementById(p, id) {
				for (var i = 0; i < p.elements.length; i++) {
					var data = p.elements[i];
					if(data.id == id)return data;
				}
			}

			var codec = new Codec('cpmproject', {
				name: 'Customizable Player Models Project',
				extension: 'cpmproject',
				remember: true,
				load_filter: {
					type: 'text',
					extensions: ['cpmproject']
				},
				compile(options) {
					let all_groups = getAllGroups();
					let box_uv = Project.box_uv;
					let this0 = this;
					let warnings = [];

					Cube.all.forEach(cube => {
						if (cube.parent == 'root') warnings.push("Loose cubes are not supported: " + cube.name);
					});
					
					let p = {};
					p.version = 1;
					p.skinSize = {};
					p.skinSize.x = Project.texture_width;
					p.skinSize.y = Project.texture_height;
					p.removeArmorOffset = true;
					p.elements = [];
					
					let textureIds = {};
					if (Texture.all.length == 1) {
						textureIds[Texture.all[0].uuid] = "skin";
					} else {
						for (var v of Texture.all) {
							var texType = KNOWN_TEXTURES[v.name];
							if (texType) {
								textureIds[v.uuid] = v.name;
								if(texType == 1) {
									let texSize = {};
									texSize.x = Project.texture_width;
									texSize.y = Project.texture_height;
									p[v.name + "Size"] = texSize;
								} else if(texType == 2) {
									p[v.name + "Tex"] = true;
								}
							}
						}
					}
					
					all_groups.slice().forEach(group => {
						let subgroups = [];
						let group_i = all_groups.indexOf(group);
						group.children.forEachReverse(cube => {
							if (cube instanceof Cube == false || !cube.export) return;
							if (!cube.rotation.allEqual(0)) {
								let sub = subgroups.find(s => {
									if (!s.rotation.equals(cube.rotation)) return false;
									if (s.rotation.filter(n => n).length > 1) {
										return s.origin.equals(cube.origin)
									} else {
										for (var i = 0; i < 3; i++) {
											if (s.rotation[i] == 0 && s.origin[i] != cube.origin[i]) return false;
										}
										return true;
									}
								});
								if (!sub) {
									sub = new Group({
										rotation: cube.rotation,
										origin: cube.origin,
										name: `${cube.name}_r1`
									})
									sub.parent = group;
									sub.is_rotation_subgroup = true;
									sub.createUniqueName(all_groups)
									subgroups.push(sub)
									group_i++;
									all_groups.splice(group_i, 0, sub);
								}
								sub.children.push(cube);
							}
						});
					});

					let modelTree = {};
					let zip = new JSZip();
					
					function convert(elem, cube) {
						if (box_uv) {
							elem.u = cube.uv_offset[0];
							elem.v = cube.uv_offset[1];
							elem.mirror = cube.mirror_uv;
						} else {
							elem.faceUV = {};
							for (var faceName of Object.keys(cube.faces)) {
								let face = cube.faces[faceName];

								if (faceName == "east") faceName = "west";
								else if (faceName == "west") faceName = "east";

								if (face.uv_size[0] != 0 && face.uv_size[1] != 0) {
									let f = {};
									elem.faceUV[faceName] = f;
									f.sx = face.uv[0];
									f.sy = face.uv[1];
									f.ex = face.uv[2];
									f.ey = face.uv[3];
									if (faceName == "up" || faceName == "down")
										f.rot = ((face.rotation + 180) % 360).toString();
									else
										f.rot = face.rotation.toString();
									f.autoUV = cube.autouv ? true : false;
								}
							}
							elem.u = 0;
							elem.v = 0;
							elem.mirror = false;
						}
						elem.mcScale = cube.inflate;
						elem.offset = { x: group.origin[0] - cube.to[0], y: -cube.from[1] - cube.size(1) + group.origin[1], z: cube.from[2] - group.origin[2] };
						elem.size = { x: cube.size(0), y: cube.size(1), z: cube.size(2) };
						if (Texture.all.length > 1) {
							let usedTexture = null;
							for (var faceName of Object.keys(cube.faces)) {
								let face = cube.faces[faceName];
								if (usedTexture && usedTexture != face.texture) warnings.push("Using multiple textures in one cube isn't supported (" + cube.name + ")");
								usedTexture = face.texture;
							}
							if (usedTexture) {
								let root = findRoot(cube);
								let id = NAME_MAP[root.name] || root.name;
								if (VANILLA_VALUES[id]) {
									let tex = VANILLA_VALUES[id].texture || "skin";
									if (textureIds[usedTexture] != tex)
										warnings.push("Unknown texture for '" + cube.name + "'");
								}
							}
						}
					}
					
					function applyNameEffects(elem) {
						var ind = elem.name.lastIndexOf('|');
						if(ind > 0){
							var dtb = elem.name.substring(ind + 1);
							if(dtb.startsWith("CPM:")) {
								elem.name = elem.name.substring(0, ind);
								try {
									let extDt = JSON.parse(base64Decode(dtb.substring(4)));
									if(extDt.extrude) {
										elem.extrude = true;
										elem.u = extDt.extrude.u;
										elem.v = extDt.extrude.v;
										elem.textureSize = extDt.extrude.ts;
										delete elem.faceUV;
									}
									if(extDt.glow)elem.glow = true;
									if(extDt.recolor) {
										elem.recolor = true;
										elem.color = extDt.color;
									}
									if(extDt.item)elem.itemRenderer = extDt.item;
								} catch(e) {
									warnings.push("Error parsing additional data: " + e);
								}
							}
						}
					}
					let storeIDObjects = {};
					let storeIDData = {};
					let partsFound = {};
					for (var group of all_groups) {
						if ((group instanceof Group === false) || !group.export) continue;
						if (group.parent instanceof Group) continue;
						if(group.name == CPM_DT_GROUP) {
							for(var dtg of group.children) {
								var sp = dtg.name.split("_");
								try {
									let extDt = JSON.parse(base64Decode(sp[sp.length - 1]));
									if(extDt.export.mode == "ids") {
										storeIDData = extDt.data;
									} else if(extDt.export.mode == "store") {
										zip.file(extDt.export.file, JSON.stringify(extDt.data, null, "  "));
									} else if(extDt.export.mode == "conf") {
										for (var key in extDt.data) { p[key] = extDt.data[key]; }
									}
								} catch(e) {
									warnings.push("Error parsing additional data: " + e);
								}
							}
							continue;
						}
						let part = {};
						let groupName = group.name;
						if(groupName.endsWith("_dup")) {
							part.dup = true;
							groupName = groupName.substring(0, groupName.length - 4);
						}
						groupName = NAME_MAP[groupName] || groupName;
						if (!VANILLA_VALUES[groupName]) {
							warnings.push("Unknown root group: " + group.name);
							continue;
						}
						if(!part.dup && !VANILLA_VALUES[groupName].custom) {
							if(partsFound[groupName]) {
								warnings.push("Duplicated root group without marking: " + group.name + "<br>    Add '_dup' to the end of the group name");
								continue;
							}
							partsFound[groupName] = true;
						}
						part.id = groupName;
						p.elements.push(part);
						part.show = false;
						var origin = group.origin.slice();
						origin[0] *= -1;
						origin[1] *= -1;
						origin[1] += 24;
						part.rotation = { x: -group.rotation[0], y: -group.rotation[1], z: group.rotation[2] };
						if (VANILLA_VALUES[part.id]) {
							var val = VANILLA_VALUES[part.id];
							origin[0] -= val.pos.x;
							origin[1] -= val.pos.y;
							origin[2] -= val.pos.z;
							if(val.rotation) {
								part.rotation.x -= val.rotation.x;
								part.rotation.y -= val.rotation.y;
								part.rotation.z -= val.rotation.z;
							}
							let cpy = val.copyFrom ? findElementById(p, val.copyFrom) : null;
							if(cpy) {
								part.rotation.x -= cpy.rotation.x;
								part.rotation.y -= cpy.rotation.y;
								part.rotation.z -= cpy.rotation.z;
								origin[0] -= cpy.pos.x;
								origin[1] -= cpy.pos.y;
								origin[2] -= cpy.pos.z;
							}
							part.customPart = val.custom;
						}
						part.pos = { x: origin[0], y: origin[1], z: origin[2] };
						part.children = [];
						modelTree[group.uuid] = part.children;
						for (var cube of group.children) {
							if (cube instanceof Cube === false || !cube.export || (!cube.rotation.allEqual(0) && !group.is_rotation_subgroup)) continue;
							let elem = {};
							part.children.push(elem);
							elem.name = cube.name;
							elem.rotation = { x: 0, y: 0, z: 0 };
							elem.textureSize = 1;
							elem.texture = true;
							elem.show = true;
							convert(elem, cube);
							elem.pos = { x: 0, y: 0, z: 0 };
							elem.color = "0";
							applyNameEffects(elem);
						}
					}
					
					for (var group of all_groups) {
						if ((group instanceof Group === false) || !group.export) continue;
						if (!(group.parent instanceof Group)) continue;
						if(group.parent.name == CPM_DT_GROUP)continue;
						let elem = {};
						elem.name = group.name;
						elem.rotation = { x: -group.rotation[0], y: -group.rotation[1], z: group.rotation[2] };
						var origin = group.origin.slice();
						if (group.parent instanceof Group) {
							origin.V3_subtract(group.parent.origin)
						}
						origin[0] *= -1;
						origin[1] *= -1;
						if (group.parent instanceof Group === false) {
							origin[1] += 24
						}
						elem.pos = { x: origin[0], y: origin[1], z: origin[2] };
						elem.textureSize = 1;
						elem.texture = true;
						elem.color = "0";
						applyNameEffects(elem);
						let e;
						modelTree[group.uuid] = [];
						for (var cube of group.children) {
							if (cube instanceof Cube === false || !cube.export || (!cube.rotation.allEqual(0) && !group.is_rotation_subgroup)) continue;
							e = jQuery.extend(true, {}, elem);
							convert(e, cube);
							e.show = cube.visibility;
							e.name = cube.name;
							applyNameEffects(e);
							if(!modelTree[group.parent.uuid]) {
								warnings.push("Skipped cube: " + group.name + "/" + cube.name + ". Check skipped roots");
								continue;								
							}
							modelTree[group.parent.uuid].push(e);
							storeIDObjects[cube.uuid] = e;
						}
						if (!e) {
							e = jQuery.extend(true, {}, elem);
							e.u = 0;
							e.v = 0;
							e.mcScale = 0;
							e.mirror = false;
							e.show = true;
							e.size = { x: 0, y: 0, z: 0 };
							if(!modelTree[group.parent.uuid]) {
								warnings.push("Skipped cube: " + group.name + "/" + cube.name + ". Check skipped roots");
								continue;								
							}
							modelTree[group.parent.uuid].push(e);
						}
						if(!modelTree[group.uuid]) {
							warnings.push("Skipped adding children: " + group.name + ". Check skipped roots");
							e.children = [];
							continue;								
						}
						e.children = modelTree[group.uuid];
					}
					
					var objectDeleteWarnSent = false;
					for(var id of Object.keys(storeIDData)) {
						var uuid = storeIDData[id];
						if(storeIDObjects[uuid])storeIDObjects[uuid].storeID = Number.parseInt(id);
						else if(!objectDeleteWarnSent) {
							objectDeleteWarnSent = true;
							warnings.push("Imported objects have been deleted, animations may not load correctly in the editor");
						}
					}
					
					zip.file("config.json", JSON.stringify(p, undefined, "  "));
					let waitForMe = [];
					function addTexture(name, tex) {
						waitForMe.push(new Promise(function(resolve) {
							fetch(tex.source)
								.then(response => response.blob())
								.then(blob => new Promise((resolve, reject) => {
									const reader = new FileReader();
									reader.onloadend = () => resolve(reader.result);
									reader.onerror = reject;
									reader.readAsDataURL(blob);
								})).then(dataUrl => {
									zip.file(name, dataUrl.substr(dataUrl.indexOf(',') + 1), { base64: true });
									resolve()
								});
						}));
					}
					
					if (Texture.all.length == 0) {
						warnings.push("No texture for the model");
					} else if (Texture.all.length == 1) {
						addTexture("skin.png", Texture.all[0]);
					} else {
						for (var v of Texture.all) {
							if (!KNOWN_TEXTURES[v.name]) {
								warnings.push("Unknown texture: " + v.name);
							} else if (KNOWN_TEXTURES[v.name] == 1) {
								addTexture(v.name + ".png", v);
							}
						}
					}
					
					let finish = async function() {
						await Promise.all(waitForMe);
						let event = {
							model: zip.generateAsync({
								type: "blob",
								compression: "DEFLATE",
								compressionOptions: {
									level: 9
								}
							}), options
						};
						this0.dispatchEvent('compile', event);
						return event.model;
					};
					if (warnings.length > 0) {
						return new Promise((resolve, reject) => {
							warnDialog.lines = ["Warning some parts of the model couldn't be exported properly:"];
							for (let w of warnings) {
								warnDialog.lines.push("<br>" + w);
							}
							warnDialog.lines.push("<br>Are you sure you want to continue?");
							warnDialog.onCancel = () => reject("Cancelled");
							warnDialog.onConfirm = () => finish().then(resolve);
							warnDialog.show();
						});
					} else {
						return finish();
					}
				},
				write(content, path) {
					var scope = codec;
					if (fs.existsSync(path) && this.overwrite) {
						this.overwrite(content, path, path => scope.afterSave(path))
					} else {
						Blockbench.writeFile(path, { content, savetype: "zip" }, path => scope.afterSave(path));
					}
				},
				export() {
					console.log("Export");
					var scope = codec;
					try {
						scope.compile().then(content => {
							Blockbench.export({
								resource_id: 'model',
								type: scope.name,
								extensions: [scope.extension],
								name: scope.fileName(),
								startpath: scope.startPath(),
								content,
								custom_writer: isApp ? (a, b) => scope.write(a, b) : null,
							}, path => scope.afterDownload(path));
						});
					} catch (exc) {
						console.error(exc);
						errorDialog.lines = ["Error while exporting:<br>", exc, "<br>Please report this on GitHub"];
						errorDialog.show();
					}
				},
				parse(arraybuffer) {
					this.dispatchEvent('parse', { arraybuffer });
					var loadedZip = new JSZip().loadAsync(arraybuffer);
					loadedZip.then(zip => {
						let textureFutures = {};
						for (var tex of Object.keys(KNOWN_TEXTURES)) {
							let f = {};
							f.tex = tex;
							f.promise = new Promise(function(resolve, reject) {
								f.resolve = function(t) {
									resolve(t);
								};
							});
							f.addCube = function(cube) {
								this.promise.then(function(t) {
									cube.applyTexture(t, true);
								});
								this.requested = true;
							}
							textureFutures[tex] = f;
						}
						
						let extraDataElements = {};
						let loadingData = [];
						let storeIDtoUUID = {};
						
						loadingData.push(zip.file("config.json").async("string").
							then(json => {
								var p = JSON.parse(json);
								Project.texture_width = p.skinSize.x;
								Project.texture_height = p.skinSize.y;
								Project.box_uv = true;

								function boxToPFUV(cube, texSc = 1, scX = 1, scY = 1, scZ = 1) {
									var dx = ceil((cube.to[0] - cube.from[0]) * texSc / (!scX ? 1 : scX));
									var dy = ceil((cube.to[1] - cube.from[1]) * texSc / (!scY ? 1 : scY));
									var dz = ceil((cube.to[2] - cube.from[2]) * texSc / (!scZ ? 1 : scZ));
									var u = floor(cube.uv_offset[0] * texSc);
									var v = floor(cube.uv_offset[1] * texSc);
									var f4 = u;
									var f5 = u + dz;
									var f6 = u + dz + dx;
									var f7 = u + dz + dx + dx;
									var f8 = u + dz + dx + dz;
									var f9 = u + dz + dx + dz + dx;
									var f10 = v;
									var f11 = v + dz;
									var f12 = v + dz + dy;
									cube.faces.up.uv = [f6, f11, f5, f10];
									cube.faces.down.uv = [f7, f10, f6, f11];
									cube.faces.east.uv = [f4, f11, f5, f12];
									cube.faces.north.uv = [f5, f11, f6, f12];
									cube.faces.west.uv = [f6, f11, f8, f12];
									cube.faces.south.uv = [f8, f11, f9, f12];
									if (cube.mirror_uv) {
										for (var faceName of Object.keys(cube.faces)) {
											let face = cube.faces[faceName];
											var t = face.uv[0];
											face.uv[0] = face.uv[2];
											face.uv[2] = t;
										}
										var uv = cube.faces.east.uv;
										cube.faces.east.uv = cube.faces.west.uv;
										cube.faces.west.uv = uv;
									}
									cube.autouv = 0;
								}

								function loadChildren(data, parent, texName) {
									for (var i = 0; i < data.length; i++) {
										var e = data[i];
										var gr = new Group({
											name: e.name,
											origin: [0, 24, 0],
										}).init();
										gr.extend({ origin: [-e.pos.x, 24 - e.pos.y, e.pos.z] });
										gr.origin.V3_add(parent.origin)
										gr.origin[1] -= 24;
										gr.addTo(parent);
										gr.extend({
											rotation: [
												-e.rotation.x,
												-e.rotation.y,
												e.rotation.z,
											]
										});
										gr.visibility = e.show;
										var cubeName = e.name;
										var extDt = {};
										if(e.extrude)extDt.extrude = {u: e.u, v: e.v, ts: e.textureSize};
										if(e.glow)extDt.glow = true;
										if(e.recolor) {
											extDt.recolor = true;
											extDt.color = e.color;
										}
										if(e.itemRenderer)extDt.item = e.itemRenderer;
										if(Object.keys(extDt).length != 0) {
											cubeName = cubeName + "|CPM:" + base64Encode(JSON.stringify(extDt));
										}
										var cube = new Cube({
											name: cubeName,
											from: [
												gr.origin[0] - e.offset.x - e.size.x * e.scale.x,
												gr.origin[1] - e.offset.y - e.size.y * e.scale.y,
												gr.origin[2] + e.offset.z
											],
											inflate: e.mcScale,
											mirror_uv: e.mirror,
											uv_offset: [e.u, e.v],
											visibility: e.show
										});
										cube.extend({
											to: [
												cube.from[0] + e.size.x * e.scale.x,
												cube.from[1] + e.size.y * e.scale.y,
												cube.from[2] + e.size.z * e.scale.z,
											]
										});
										if (e.faceUV || e.textureSize > 1 || e.singleTex || e.scale.x != 1 || e.scale.y != 1 || e.scale.z != 1) {
											if (Project.box_uv) {
												Project.box_uv = false;
												Cube.all.forEach(c => boxToPFUV(c));
											}
											cube.autouv = 0;
											if (e.faceUV) {
												for (var faceName of Object.keys(cube.faces)) {
													let face = cube.faces[faceName];

													if (faceName == "east") faceName = "west";
													else if (faceName == "west") faceName = "east";

													if (e.faceUV[faceName]) {
														var ef = e.faceUV[faceName];
														face.uv = [ef.sx, ef.sy, ef.ex, ef.ey];
														if (faceName == "up" || faceName == "down")
															face.rotation = ((ef.rot + 180) % 360);
														else
															face.rotation = ef.rot;
													} else {
														face.uv = [0, 0, 0, 0];
													}
												}
											} else if (e.singleTex || e.extrude) {
												if (e.mcScale == 0 && (e.size.x == 0 || e.size.y == 0 || e.size.z == 0)) {
													var texU = e.u * e.textureSize;
													var texV = e.v * e.textureSize;
													if (e.size.x == 0) {
														var tu = texU + ceil(e.size.z * e.textureSize);
														var tv = texV + ceil(e.size.y * e.textureSize);
														for (var faceName of Object.keys(cube.faces)) {
															let face = cube.faces[faceName];
															if (faceName == "west") {
																face.uv = [texU, texV, tu, tv];
															} else if (faceName == "east") {
																face.uv = [tu, texV, texU, tv];
															} else {
																face.uv = [0, 0, 0, 0];
															}
														}
													} else if (e.size.y == 0) {
														var tu = texU + ceil(e.size.x * e.textureSize);
														var tv = texV + ceil(e.size.z * e.textureSize);
														for (var faceName of Object.keys(cube.faces)) {
															let face = cube.faces[faceName];
															if (faceName == "up") {
																face.uv = [tu, texV, texU, tv];
															} else if (faceName == "down") {
																face.uv = [texU, texV, tu, tv];
															} else {
																face.uv = [0, 0, 0, 0];
															}
														}
													} else if (e.size.z == 0) {
														var tu = texU + ceil(e.size.x * e.textureSize);
														var tv = texV + ceil(e.size.y * e.textureSize);
														for (var faceName of Object.keys(cube.faces)) {
															let face = cube.faces[faceName];
															if (faceName == "north") {
																face.uv = [texU, texV, tu, tv];
															} else if (faceName == "south") {
																face.uv = [tu, texV, texU, tv];
															} else {
																face.uv = [0, 0, 0, 0];
															}
														}
													}
												} else {
													var size = Math.max(e.size.x, e.size.y, e.size.z);
													for (var faceName of Object.keys(cube.faces)) {
														let face = cube.faces[faceName];
														face.uv[0] = e.u * e.textureSize;
														face.uv[1] = e.v * e.textureSize;
														face.uv[2] = (e.u + size) * e.textureSize;
														face.uv[3] = (e.v + size) * e.textureSize;
													}
												}
												if (e.mirror) {
													for (var faceName of Object.keys(cube.faces)) {
														let face = cube.faces[faceName];
														var t = face.uv[0];
														face.uv[0] = face.uv[2];
														face.uv[2] = t;
													}
													var uv = cube.faces.east.uv;
													cube.faces.east.uv = cube.faces.west.uv;
													cube.faces.west.uv = uv;
												}
											} else {
												boxToPFUV(cube, e.textureSize, e.scale.x, e.scale.y, e.scale.z);
											}
										} else if (!Project.box_uv) {
											boxToPFUV(cube);
										}
										cube.addTo(gr).init();
										textureFutures[texName].addCube(cube);
										if (e.children) {
											loadChildren(e.children, gr, texName);
										}
										storeIDtoUUID[e.storeID] = cube.uuid;
									}
								}
								
								for (var i = 0; i < p.elements.length; i++) {
									var data = p.elements[i];
									var vals = VANILLA_VALUES[data.id];
									if (vals) {
										let rx = data.rotation.x;
										let ry = data.rotation.y;
										let rz = data.rotation.z;
										let px = vals.pos.x + data.pos.x;
										let py = vals.pos.y + data.pos.y;
										let pz = vals.pos.z + data.pos.z;
										let cpy = vals.copyFrom ? findElementById(p, vals.copyFrom) : null;
										if(cpy) {
											rx += cpy.rotation.x;
											ry += cpy.rotation.y;
											rz += cpy.rotation.z;
											px += cpy.pos.x;
											py += cpy.pos.y;
											pz += cpy.pos.z;
										}
										var groupName = data.id;
										if(data.dup)groupName += "_dup";
										var gr = new Group({
											name: groupName,
											origin: [0, 24, 0],
											rotation: [rx, ry, rz]
										}).init();
										gr.extend({ origin: [-px, 24 - py, pz] });

										let texName = vals.texture || "skin";
										
										if (data.show) {
											var cube = new Cube({
												name: data.id,
												origin: gr.origin,
												from: [
													gr.origin[0] - vals.offset.x - vals.size.x,
													gr.origin[1] - vals.offset.y - vals.size.y,
													gr.origin[2] + vals.offset.z
												],
												rotation: [0, 0, 0],
												uv_offset: [vals.uv.u, vals.uv.v]
											});
											cube.extend({
												to: [
													cube.from[0] + vals.size.x,
													cube.from[1] + vals.size.y,
													cube.from[2] + vals.size.z,
												]
											});
											cube.addTo(gr).init();
											textureFutures[texName].addCube(cube);
											if (!Project.box_uv) {
												boxToPFUV(cube);
											}
										}

										if (data.children) {
											loadChildren(data.children, gr, texName);
										}
									}
								}
								
								for (var tex of Object.keys(KNOWN_TEXTURES)) {
									if(KNOWN_TEXTURES[tex] == 2 && textureFutures[tex].requested) {
										texture = new Texture({mode: 'bitmap', name: tex});
										loadResizedTexture(tex, texture);
										texture.add(false);
										textureFutures[tex].resolve(texture);
									}
								}
								
								if(p.textures) {
									var dt = {};
									extraDataElements["textures"] = dt;
									dt.export = {mode: "conf"};
									dt.data = {textures: p.textures};
								}
								
								var dt = {};
								extraDataElements["store"] = dt;
								dt.export = {mode: "ids"};
								dt.data = storeIDtoUUID;
								
								dt = {};
								extraDataElements["properties"] = dt;
								dt.export = {mode: "conf"};
								dt.data = {};
								dt.data.hideHeadIfSkull = p.hideHeadIfSkull;
								dt.data.removeArmorOffset = p.removeArmorOffset;
								
								if(p.scaling && p.scaling != 0) {
									dt = {};
									extraDataElements["scaling"] = dt;
									dt.export = {mode: "conf"};
									dt.data = {};
									dt.data.scaling = p.scaling;
									if(p.scalingEx) {
										dt.data.scalingEx = p.scalingEx;
									}
								}

								this.dispatchEvent('parsed', { arraybuffer });
							}
						).then(() => {
							for (var fileName of Object.keys(zip.files)) {
								if((fileName.startsWith("animations/") && !fileName.endsWith("/")) || fileName == "description.json" || fileName == "anim_enc.json") {
									let fname = fileName;
									loadingData.push(zip.file(fileName).async("string").
										then(json => {
											var p = JSON.parse(json);
											var dt = {};
											extraDataElements[fname] = dt;
											dt.export = {mode: "store", file: fname};
											dt.data = p;
										}
									));
								}
							}
						}));
						for (var tex of Object.keys(KNOWN_TEXTURES)) {
							let ftex = tex;
							if(KNOWN_TEXTURES[tex] == 1) {
								var zipFile = zip.file(tex + ".png");
								if(zipFile) {
									loadingData.push(zipFile.async("base64").then(function(img) {
										var texture = new Texture().fromDataURL('data:image/png;base64,' + img);
										texture.name = ftex;
										texture.add();
										textureFutures[ftex].resolve(texture);
									}));
								}
							}
						}
						Promise.all(loadingData).then(async function() {await Promise.all(loadingData);}).then(() => {
							let dtGroup = new Group({
								name: CPM_DT_GROUP
							}).init();
							for (var elemName of Object.keys(extraDataElements)) {
								let dt = new Cube({
									name: "data_" + elemName + "_" + base64Encode(JSON.stringify(extraDataElements[elemName])),
									from: [0, 0, 0],
									to: [0, 0, 0]
								}).init();
								dt.addTo(dtGroup);
							}
							
							loadTextureDraggable()
							Canvas.updateAllBones()
							Canvas.updateVisibility()
							setProjectTitle()
							updateSelection()
						})
					});
				}
			});
			const format = new ModelFormat({
				id: 'cpm',
				icon: 'icon-player',
				name: "Customizable Player Models model",
				description: "",
				bone_rig: true,
				box_uv: true,
				optional_box_uv: true,
				centered_grid: true,
				single_texture: false,
				rotate_cubes: true,
				codec
			});
			codec.format = format;
			format.new = function() {
				skin_dialog.show();
				return true;
			};
			const skin_dialog = new Dialog({
				title: tl('dialog.skin.title'),
				id: 'image_editor',
				form: {
					resolution: {
						label: 'dialog.create_texture.resolution', type: 'select', value: 64, options: {
							64: '64x64',
							128: '128x128',
							256: '256x256'
						}
					},
					texture: {
						label: 'dialog.skin.texture',
						type: 'file',
						extensions: ['png'],
						readtype: 'image',
						filetype: 'PNG',
					}
				},
				draggable: true,
				onConfirm(result) {
					if (newProject(format)) {
						Project.texture_width = result.resolution;
						Project.texture_height = result.resolution;
						for (var partName of Object.keys(VANILLA_VALUES)) {
							let vals = VANILLA_VALUES[partName];
							if (vals.custom) continue;
							if (vals) {
								var gr = new Group({
									name: partName,
									origin: [0, 24, 0]
								}).init();
								gr.extend({ origin: [-vals.pos.x, 24 - vals.pos.y, vals.pos.z] });
								gr.isOpen = true;
								var cube = new Cube({
									name: partName,
									origin: gr.origin,
									from: [
										gr.origin[0] - vals.offset.x - vals.size.x,
										gr.origin[1] - vals.offset.y - vals.size.y,
										gr.origin[2] + vals.offset.z
									],
									rotation: [0, 0, 0],
									uv_offset: [vals.uv.u, vals.uv.v]
								});
								cube.extend({
									to: [
										cube.from[0] + floor(vals.size.x),
										cube.from[1] + floor(vals.size.y),
										cube.from[2] + floor(vals.size.z),
									]
								});
								cube.addTo(gr).init();
							}
						}
						let texture;
						if (result.texture) {
							texture = new Texture().fromPath(result.texture);
							texture.name = "skin";
							texture.add(false);
						} else {
							texture = generateTemplate(
								Project.texture_width,
								Project.texture_height
							);
						}
						Cube.all.forEach(c => c.applyTexture(texture));
						loadTextureDraggable()
						Canvas.updateAllBones()
						Canvas.updateVisibility()
						setProjectTitle()
						updateSelection()
					}
					this.hide();
				},
				onCancel() {
					this.hide();
					Format = 0;
				}
			});

			function generateTemplate(width = 64, height = 64) {
				var texture = new Texture({
					mode: 'bitmap',
					name: 'skin'
				})

				var canvas = document.createElement('canvas')
				var ctx = canvas.getContext('2d');
				canvas.width = width;
				canvas.height = height;

				Cube.all.forEach((cube) => {
					TextureGenerator.paintCubeBoxTemplate(cube, texture, canvas, null, false);
				});
				ctx.fillStyle = '#cdefff';
				ctx.fillRect(9, 11, 2, 2);
				ctx.fillRect(13, 11, 2, 2);
				var dataUrl = canvas.toDataURL();
				texture.fromDataURL(dataUrl).add(false);
				return texture;
			}

			import_action = new Action('import_cpmproject', {
				name: 'Open Customizable Player Models Project',
				description: '',
				icon: 'icon-player',
				category: 'file',
				click() {
					Blockbench.import({
						extensions: ['cpmproject'],
						type: 'Customizable Player Models Project',
						readtype: 'binary',
						resource_id: 'cpmproject_files'
					}, files => {
						newProject(Formats.cpm);
						try {
							codec.parse(files[0].content);
						} catch (exc) {
							console.error(exc);
							errorDialog.lines = ["Error while importing:<br>", exc, "<br>Please report this on GitHub"];
							errorDialog.show();
							return;
						}
						csname = files[0].name.replace(".cpmproject", "").replace(/\s+/g, "_").toLowerCase();
						Project.name = csname;
						Project.geometry_name = csname;
					})
				}
			})
			export_action = new Action('export_cpmproject', {
				name: 'Export Customizable Player Models Project',
				description: '',
				icon: 'icon-player',
				category: 'file',
				click() {
					codec.export();
				}
			});

			MenuBar.addAction(import_action, 'file.5')
			MenuBar.addAction(export_action, 'file.export')
			add_more_parts = new Action('add_parts', {
				name: 'Add Parts',
				description: '',
				icon: 'icon-player',
				category: 'edit',
				condition: { formats: ['cpm'] },
				children() {
					var r = [];
					r.push({icon: 'layers', name: 'Second Layer', click() {
						Undo.initEdit({elements: []});
						let newParts = [];
						let texToUse = Texture.all[0];
						for (var t of Texture.all) {
							if(t.name == "skin") {
								texToUse = t;
								break;
							}
						}
						for (var partName of Object.keys(VANILLA_VALUES)) {
							let vals = VANILLA_VALUES[partName];
							if (vals.custom) continue;
							if (vals) {
								var gr = null;
								for(var g of Group.all) {
									if(g.name == partName) {
										gr = g;
										break;
									}	
								}
								if(!gr)continue;
								var cube = new Cube({
									name: vals.layer,
									origin: gr.origin,
									from: [
										gr.origin[0] - vals.offset.x - vals.size.x,
										gr.origin[1] - vals.offset.y - vals.size.y,
										gr.origin[2] + vals.offset.z
									],
									rotation: [0, 0, 0],
									uv_offset: [vals.layerUV.u, vals.layerUV.v]
								});
								cube.extend({
									to: [
										cube.from[0] + floor(vals.size.x),
										cube.from[1] + floor(vals.size.y),
										cube.from[2] + floor(vals.size.z),
									]
								});
								cube.inflate = 0.25;
								cube.addTo(gr).init();
								cube.applyTexture(texToUse);
								newParts.push(cube);
							}
							Undo.finishEdit('Add Second Layer', {elements: newParts});
						}
					}});
					
					for(var grN of Object.keys(ROOT_GROUPS)) {
						var rgr = ROOT_GROUPS[grN];
						r.push({icon: rgr.icon, name: rgr.display, grId: grN, click() {
							Undo.initEdit({elements: [], textures: []});
							var rgr = ROOT_GROUPS[this.grId];
							let newParts = [];
							let newTextures = [];
							for (var partName of Object.keys(rgr.parts)) {
								let texName = rgr.parts[partName];
								let vals = VANILLA_VALUES[partName];
								var gr = null;
								for(var g of Group.all) {
									if(g.name == partName) {
										gr = g;
										break;
									}	
								}
								if(!gr) {
									gr = new Group({
										name: partName,
										origin: [0, 24, 0]
									}).init();
									gr.extend({ origin: [-vals.pos.x, 24 - vals.pos.y, vals.pos.z] });
									gr.isOpen = true;
								}
								var cube = new Cube({
									name: partName,
									origin: gr.origin,
									from: [
										gr.origin[0] - vals.offset.x - vals.size.x,
										gr.origin[1] - vals.offset.y - vals.size.y,
										gr.origin[2] + vals.offset.z
									],
									rotation: [0, 0, 0],
									uv_offset: [vals.uv.u, vals.uv.v]
								});
								cube.mirror_uv = vals.mirror;
								cube.extend({
									to: [
										cube.from[0] + floor(vals.size.x),
										cube.from[1] + floor(vals.size.y),
										cube.from[2] + floor(vals.size.z),
									]
								});
								if(vals.rotation) {
									gr.rotation[0] = -vals.rotation.x;
									gr.rotation[1] = -vals.rotation.y;
									gr.rotation[2] = vals.rotation.z;
								}
								cube.inflate = vals.mcscale;
								let texToUse;
								for(var tex of Texture.all) {
									if(tex.name == texName) {
										texToUse = tex;
										break;
									}
								}
								if(!texToUse) {
									texToUse = new Texture({mode: 'bitmap', name: texName});
									loadResizedTexture(texName, texToUse);
									texToUse.add(false);
									newTextures.push(texToUse);
								}
								cube.addTo(gr).init();
								newParts.push(cube);
								cube.applyTexture(texToUse);
							}
							Undo.finishEdit('Add ' + rgr.display, {elements: newParts, textures: newTextures});
						}});
					}
					return r;
				},
				click(event) {
					new Menu(this.children()).open(event.target);
				}
			});
			MenuBar.addAction(add_more_parts, 'edit.7');
		},
		onunload() {
			import_action.delete();
			export_action.delete();
			add_more_parts.delete();
		}
	});
})();
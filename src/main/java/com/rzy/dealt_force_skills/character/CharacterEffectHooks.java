package com.rzy.dealt_force_skills.character;

import com.rzy.dealt_force_skills.character.catdad.CatDadStateManager;
import com.rzy.dealt_force_skills.character.department.DepartmentOfTransportationStateManager;
import com.rzy.dealt_force_skills.character.dwolf.DWolfStateManager;
import com.rzy.dealt_force_skills.character.hackclaw.HackclawStateManager;
import com.rzy.dealt_force_skills.character.lexninjia.LexNinjiaStateManager;
import com.rzy.dealt_force_skills.character.manba.ManbaStateManager;
import com.rzy.dealt_force_skills.character.nikaidou.NikaidouHiroStateManager;
import com.rzy.dealt_force_skills.character.raptor.RaptorStateManager;
import com.rzy.dealt_force_skills.character.shepherd.ShepherdStateManager;
import com.rzy.dealt_force_skills.character.sineva.SinevaSkills;
import com.rzy.dealt_force_skills.character.sineva.SinevaStateManager;
import com.rzy.dealt_force_skills.character.stinger.StingerStateManager;
import com.rzy.dealt_force_skills.character.uluru.UluruStateManager;
import com.rzy.dealt_force_skills.entity.RaptorFalconDroneEntity;
import com.rzy.dealt_force_skills.entity.UluruLoiteringMissileEntity;
import com.rzy.dealt_force_skills.registry.ModEffects;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;

public final class CharacterEffectHooks {
    private CharacterEffectHooks() {
    }

    public static void onCharacterSelected(ServerPlayer player, CharacterDefinition character) {
        player.addEffect(new MobEffectInstance(ModEffects.CHARACTER_FRAMEWORK.get(), 40, 0, false, false, false));
    }

    public static void onCharacterDeselected(ServerPlayer player, CharacterDefinition character) {
        if (ModCharacters.SINEVA_ID.equals(character.id())) {
            SinevaSkills.clearForPlayer(player);
            SinevaStateManager.removeTransientModifiers(player);
            SinevaStateManager.clearState(player);
        }
        if (ModCharacters.ULURU_ID.equals(character.id())) {
            UluruLoiteringMissileEntity.stopPlayerControl(player);
            UluruStateManager.clearState(player);
        }
        if (ModCharacters.D_WOLF_ID.equals(character.id())) {
            DWolfStateManager.removeTransientModifiers(player);
            DWolfStateManager.clearState(player);
        }
        if (ModCharacters.SHEPHERD_ID.equals(character.id())) {
            ShepherdStateManager.clearState(player);
        }
        if (ModCharacters.STINGER_ID.equals(character.id())) {
            StingerStateManager.clearState(player);
        }
        if (ModCharacters.MANBA_ID.equals(character.id())) {
            ManbaStateManager.clearState(player);
        }
        if (ModCharacters.NIKAIDOU_HIRO_ID.equals(character.id())) {
            NikaidouHiroStateManager.clearState(player);
        }
        if (ModCharacters.CATDAD_ID.equals(character.id())) {
            CatDadStateManager.clearState(player);
        }
        if (ModCharacters.DEPARTMENT_OF_TRANSPORTATION_ID.equals(character.id())) {
            DepartmentOfTransportationStateManager.clearState(player);
        }
        if (ModCharacters.HACKCLAW_ID.equals(character.id())) {
            HackclawStateManager.clearState(player);
        }
        if (ModCharacters.RAPTOR_ID.equals(character.id())) {
            RaptorFalconDroneEntity.stopPlayerControl(player);
            RaptorStateManager.clearRuntimeOnDeath(player);
            RaptorStateManager.clearState(player);
        }
        if (ModCharacters.LEX_NINJIA_ID.equals(character.id())) {
            LexNinjiaStateManager.onDeselected(player);
        }
        player.removeEffect(ModEffects.CHARACTER_FRAMEWORK.get());
    }

    public static void tick(ServerPlayer player, CharacterDefinition character) {
        // Future character-wide effects can be routed here.
    }
}

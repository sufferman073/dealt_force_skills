package com.rzy.dealt_force_skills.skill;

import com.rzy.dealt_force_skills.character.CharacterDefinition;
import com.rzy.dealt_force_skills.character.CharacterEffectHooks;
import com.rzy.dealt_force_skills.character.CharacterSelectionManager;
import com.rzy.dealt_force_skills.character.ModCharacters;
import com.rzy.dealt_force_skills.character.SkillDefinition;
import com.rzy.dealt_force_skills.character.SkillSlot;
import com.rzy.dealt_force_skills.character.catdad.CatDadSkills;
import com.rzy.dealt_force_skills.character.catdad.CatDadStateManager;
import com.rzy.dealt_force_skills.character.department.DepartmentOfTransportationSkills;
import com.rzy.dealt_force_skills.character.department.DepartmentOfTransportationStateManager;
import com.rzy.dealt_force_skills.character.electronics.ElectronicInterferenceManager;
import com.rzy.dealt_force_skills.character.dwolf.DWolfSkills;
import com.rzy.dealt_force_skills.character.dwolf.DWolfStateManager;
import com.rzy.dealt_force_skills.character.gizmo.GizmoSkills;
import com.rzy.dealt_force_skills.character.gizmo.GizmoStateManager;
import com.rzy.dealt_force_skills.character.hackclaw.HackclawSkills;
import com.rzy.dealt_force_skills.character.hackclaw.HackclawStateManager;
import com.rzy.dealt_force_skills.character.lexninjia.LexNinjiaSkills;
import com.rzy.dealt_force_skills.character.lexninjia.LexNinjiaStateManager;
import com.rzy.dealt_force_skills.character.luna.LunaSkills;
import com.rzy.dealt_force_skills.character.luna.LunaStateManager;
import com.rzy.dealt_force_skills.character.manba.ManbaSkills;
import com.rzy.dealt_force_skills.character.manba.ManbaStateManager;
import com.rzy.dealt_force_skills.character.morse.MorseSkills;
import com.rzy.dealt_force_skills.character.morse.MorseStateManager;
import com.rzy.dealt_force_skills.character.nikaidou.NikaidouHiroSkills;
import com.rzy.dealt_force_skills.character.nikaidou.NikaidouHiroStateManager;
import com.rzy.dealt_force_skills.character.nox.NoxSkills;
import com.rzy.dealt_force_skills.character.nox.NoxStateManager;
import com.rzy.dealt_force_skills.character.raptor.RaptorSkills;
import com.rzy.dealt_force_skills.character.raptor.RaptorStateManager;
import com.rzy.dealt_force_skills.character.shepherd.ShepherdSkills;
import com.rzy.dealt_force_skills.character.shepherd.ShepherdStateManager;
import com.rzy.dealt_force_skills.character.sineva.SinevaSkills;
import com.rzy.dealt_force_skills.character.sineva.SinevaStateManager;
import com.rzy.dealt_force_skills.character.stinger.StingerSkills;
import com.rzy.dealt_force_skills.character.stinger.StingerStateManager;
import com.rzy.dealt_force_skills.character.tempest.TempestSkills;
import com.rzy.dealt_force_skills.character.tempest.TempestStateManager;
import com.rzy.dealt_force_skills.character.toxik.ToxikSkills;
import com.rzy.dealt_force_skills.character.toxik.ToxikStateManager;
import com.rzy.dealt_force_skills.character.undead.UndeadSkills;
import com.rzy.dealt_force_skills.character.undead.UndeadStateManager;
import com.rzy.dealt_force_skills.character.uluru.UluruSkills;
import com.rzy.dealt_force_skills.character.uluru.UluruStateManager;
import com.rzy.dealt_force_skills.character.vlinder.VlinderSkills;
import com.rzy.dealt_force_skills.character.vlinder.VlinderStateManager;
import com.rzy.dealt_force_skills.character.vyron.VyronSkills;
import com.rzy.dealt_force_skills.character.vyron.VyronStateManager;
import com.rzy.dealt_force_skills.entity.RaptorFalconDroneEntity;
import com.rzy.dealt_force_skills.entity.UluruLoiteringMissileEntity;
import com.rzy.dealt_force_skills.registry.ModEffects;
import com.rzy.dealt_force_skills.shop.LexNinjiaCurrencyManager;
import com.rzy.dealt_force_skills.shop.UndeadSoulManager;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Optional;

public final class SkillDispatcher {
    private SkillDispatcher() {
    }

    public static void onCharacterSelected(ServerPlayer player, CharacterDefinition character) {
        CharacterEffectHooks.onCharacterSelected(player, character);
        if (!ModCharacters.UNDEAD_ID.equals(character.id())) {
            UndeadStateManager.onDeselected(player);
        }
        if (!ModCharacters.LEX_NINJIA_ID.equals(character.id())) {
            LexNinjiaStateManager.onDeselected(player);
        }
        if (ModCharacters.SINEVA_ID.equals(character.id())) {
            SinevaStateManager.initializeIfNeeded(player);
            SinevaStateManager.syncToClient(player);
        }
        if (ModCharacters.ULURU_ID.equals(character.id())) {
            UluruStateManager.initializeIfNeeded(player);
            UluruStateManager.syncToClient(player);
        }
        if (ModCharacters.D_WOLF_ID.equals(character.id())) {
            DWolfStateManager.initializeIfNeeded(player);
            DWolfStateManager.syncToClient(player);
        }
        if (ModCharacters.GIZMO_ID.equals(character.id())) {
            GizmoStateManager.initializeIfNeeded(player);
            GizmoStateManager.syncToClient(player);
        }
        if (ModCharacters.SHEPHERD_ID.equals(character.id())) {
            ShepherdStateManager.initializeIfNeeded(player);
            ShepherdStateManager.syncToClient(player);
        }
        if (ModCharacters.LUNA_ID.equals(character.id())) {
            LunaStateManager.initializeIfNeeded(player);
            LunaStateManager.syncToClient(player);
        }
        if (ModCharacters.HACKCLAW_ID.equals(character.id())) {
            HackclawStateManager.initializeIfNeeded(player);
            HackclawStateManager.syncToClient(player);
        }
        if (ModCharacters.VYRON_ID.equals(character.id())) {
            VyronStateManager.initializeIfNeeded(player);
            VyronStateManager.syncToClient(player);
        }
        if (ModCharacters.NOX_ID.equals(character.id())) {
            NoxStateManager.initializeIfNeeded(player);
            NoxStateManager.syncToClient(player);
        }
        if (ModCharacters.STINGER_ID.equals(character.id())) {
            StingerStateManager.initializeIfNeeded(player);
            StingerStateManager.syncToClient(player);
        }
        if (ModCharacters.MANBA_ID.equals(character.id())) {
            ManbaStateManager.initializeIfNeeded(player);
            ManbaStateManager.syncToClient(player);
        }
        if (ModCharacters.NIKAIDOU_HIRO_ID.equals(character.id())) {
            NikaidouHiroStateManager.initializeIfNeeded(player);
            NikaidouHiroStateManager.syncToClient(player);
        }
        if (ModCharacters.CATDAD_ID.equals(character.id())) {
            CatDadStateManager.initializeIfNeeded(player);
            CatDadStateManager.syncToClient(player);
        }
        if (ModCharacters.DEPARTMENT_OF_TRANSPORTATION_ID.equals(character.id())) {
            DepartmentOfTransportationStateManager.initializeIfNeeded(player);
            DepartmentOfTransportationStateManager.syncToClient(player);
        }
        if (ModCharacters.UNDEAD_ID.equals(character.id())) {
            UndeadStateManager.initializeIfNeeded(player);
            UndeadStateManager.syncToClient(player);
            UndeadSoulManager.sync(player);
        }
        if (ModCharacters.LEX_NINJIA_ID.equals(character.id())) {
            LexNinjiaStateManager.initializeIfNeeded(player);
            LexNinjiaStateManager.syncToClient(player);
            LexNinjiaCurrencyManager.sync(player);
        }
        if (ModCharacters.MORSE_ID.equals(character.id())) {
            MorseStateManager.initializeIfNeeded(player);
            MorseStateManager.syncToClient(player);
        }
        if (ModCharacters.TOXIK_ID.equals(character.id())) {
            ToxikStateManager.initializeIfNeeded(player);
            ToxikStateManager.syncToClient(player);
        }
        if (ModCharacters.RAPTOR_ID.equals(character.id())) {
            RaptorStateManager.initializeIfNeeded(player);
            RaptorStateManager.syncToClient(player);
        }
        if (ModCharacters.VLINDER_ID.equals(character.id())) {
            VlinderStateManager.initializeIfNeeded(player);
            VlinderStateManager.syncToClient(player);
        }
        if (ModCharacters.TEMPEST_ID.equals(character.id())) {
            TempestStateManager.initializeIfNeeded(player);
            TempestStateManager.syncToClient(player);
        }
        player.displayClientMessage(
                Component.translatable("message.dealt_force_skills.character_selected",
                        Component.translatable(character.nameTranslationKey())),
                false
        );
    }

    public static void tickPlayer(ServerPlayer player) {
        CharacterSelectionManager.getSelectedCharacter(player)
                .ifPresent(character -> {
                    CharacterEffectHooks.tick(player, character);
                    if (ModCharacters.SINEVA_ID.equals(character.id())) {
                        SinevaStateManager.tick(player);
                    }
                    if (ModCharacters.ULURU_ID.equals(character.id())) {
                        UluruStateManager.tick(player);
                    }
                    if (ModCharacters.D_WOLF_ID.equals(character.id())) {
                        DWolfStateManager.tick(player);
                    }
                    if (ModCharacters.GIZMO_ID.equals(character.id())) {
                        GizmoStateManager.tick(player);
                    }
                    if (ModCharacters.SHEPHERD_ID.equals(character.id())) {
                        ShepherdStateManager.tick(player);
                    }
                    if (ModCharacters.LUNA_ID.equals(character.id())) {
                        LunaStateManager.tick(player);
                    }
                    if (ModCharacters.HACKCLAW_ID.equals(character.id())) {
                        HackclawStateManager.tick(player);
                    }
                    if (ModCharacters.VYRON_ID.equals(character.id())) {
                        VyronStateManager.tick(player);
                    }
                    if (ModCharacters.NOX_ID.equals(character.id())) {
                        NoxStateManager.tick(player);
                    }
                    if (ModCharacters.STINGER_ID.equals(character.id())) {
                        StingerStateManager.tick(player);
                        StingerSkills.tickStimLockStatus(player);
                    }
                    if (ModCharacters.MANBA_ID.equals(character.id())) {
                        ManbaStateManager.tick(player);
                    }
                    if (ModCharacters.NIKAIDOU_HIRO_ID.equals(character.id())) {
                        NikaidouHiroStateManager.tick(player);
                    }
                    if (ModCharacters.CATDAD_ID.equals(character.id())) {
                        CatDadStateManager.tick(player);
                    }
                    if (ModCharacters.DEPARTMENT_OF_TRANSPORTATION_ID.equals(character.id())) {
                        DepartmentOfTransportationStateManager.tick(player);
                    }
                    if (ModCharacters.UNDEAD_ID.equals(character.id())) {
                        UndeadStateManager.tick(player);
                    }
                    if (ModCharacters.LEX_NINJIA_ID.equals(character.id())) {
                        LexNinjiaStateManager.tick(player);
                    }
                    if (ModCharacters.MORSE_ID.equals(character.id())) {
                        MorseStateManager.tick(player);
                    }
                    if (ModCharacters.TOXIK_ID.equals(character.id())) {
                        ToxikStateManager.tick(player);
                    }
                    if (ModCharacters.RAPTOR_ID.equals(character.id())) {
                        RaptorStateManager.tick(player);
                    }
                    if (ModCharacters.VLINDER_ID.equals(character.id())) {
                        VlinderStateManager.tick(player);
                    }
                    if (ModCharacters.TEMPEST_ID.equals(character.id())) {
                        TempestStateManager.tick(player);
                    }
                });
    }

    public static boolean useSkill(ServerPlayer player, SkillSlot slot, boolean alternate) {
        if (UluruLoiteringMissileEntity.isPlayerControlling(player)
                || RaptorFalconDroneEntity.isPlayerControlling(player)) {
            return false;
        }

        if (player.hasEffect(ModEffects.STUN.get())
                || player.hasEffect(ModEffects.WEBBED.get())
                || player.hasEffect(ModEffects.TEMPEST_DISARMED.get())
                || StingerStateManager.isDowned(player)
                || VlinderStateManager.isDowned(player)
                || CatDadStateManager.isDowned(player)
                || TempestStateManager.isActionLocked(player)
                || UndeadStateManager.isRitualDancing(player)) {
            player.displayClientMessage(Component.translatable("message.dealt_force_skills.stunned"), true);
            return false;
        }

        if (!slot.canBeTriggeredByKey()) {
            return false;
        }

        Optional<CharacterDefinition> selected = CharacterSelectionManager.getSelectedCharacter(player);
        if (selected.isEmpty()) {
            player.displayClientMessage(Component.translatable("message.dealt_force_skills.select_character_first"), true);
            return false;
        }

        CharacterDefinition character = selected.get();
        Optional<SkillDefinition> skill = character.skill(slot);
        if (skill.isEmpty()) {
            player.displayClientMessage(Component.translatable("message.dealt_force_skills.skill_missing"), true);
            return false;
        }

        if (ElectronicInterferenceManager.tryBlockSkillUse(player, character, slot)) {
            return true;
        }
        MorseStateManager.recordPlayerAction(player);

        if (ModCharacters.SINEVA_ID.equals(character.id())) {
            boolean handled = SinevaSkills.useSkill(player, slot, alternate);
            SinevaStateManager.syncToClient(player);
            return handled;
        }

        if (ModCharacters.ULURU_ID.equals(character.id())) {
            boolean handled = UluruSkills.useSkill(player, slot, alternate);
            UluruStateManager.syncToClient(player);
            return handled;
        }

        if (ModCharacters.D_WOLF_ID.equals(character.id())) {
            boolean handled = DWolfSkills.useSkill(player, slot, alternate);
            DWolfStateManager.syncToClient(player);
            return handled;
        }

        if (ModCharacters.GIZMO_ID.equals(character.id())) {
            boolean handled = GizmoSkills.useSkill(player, slot, alternate);
            GizmoStateManager.syncToClient(player);
            return handled;
        }

        if (ModCharacters.SHEPHERD_ID.equals(character.id())) {
            boolean handled = ShepherdSkills.useSkill(player, slot, alternate);
            ShepherdStateManager.syncToClient(player);
            return handled;
        }

        if (ModCharacters.LUNA_ID.equals(character.id())) {
            boolean handled = LunaSkills.useSkill(player, slot, alternate);
            LunaStateManager.syncToClient(player);
            return handled;
        }

        if (ModCharacters.HACKCLAW_ID.equals(character.id())) {
            boolean handled = HackclawSkills.useSkill(player, slot, alternate);
            HackclawStateManager.syncToClient(player);
            return handled;
        }

        if (ModCharacters.VYRON_ID.equals(character.id())) {
            boolean handled = VyronSkills.useSkill(player, slot, alternate);
            VyronStateManager.syncToClient(player);
            return handled;
        }

        if (ModCharacters.NOX_ID.equals(character.id())) {
            boolean handled = NoxSkills.useSkill(player, slot, alternate);
            NoxStateManager.syncToClient(player);
            return handled;
        }

        if (ModCharacters.STINGER_ID.equals(character.id())) {
            boolean handled = StingerSkills.useSkill(player, slot, alternate);
            StingerStateManager.syncToClient(player);
            return handled;
        }

        if (ModCharacters.MANBA_ID.equals(character.id())) {
            boolean handled = ManbaSkills.useSkill(player, slot, alternate);
            ManbaStateManager.syncToClient(player);
            return handled;
        }

        if (ModCharacters.NIKAIDOU_HIRO_ID.equals(character.id())) {
            boolean handled = NikaidouHiroSkills.useSkill(player, slot, alternate);
            NikaidouHiroStateManager.syncToClient(player);
            return handled;
        }

        if (ModCharacters.CATDAD_ID.equals(character.id())) {
            boolean handled = CatDadSkills.useSkill(player, slot, alternate);
            CatDadStateManager.syncToClient(player);
            return handled;
        }

        if (ModCharacters.DEPARTMENT_OF_TRANSPORTATION_ID.equals(character.id())) {
            boolean handled = DepartmentOfTransportationSkills.useSkill(player, slot, alternate);
            DepartmentOfTransportationStateManager.syncToClient(player);
            return handled;
        }

        if (ModCharacters.UNDEAD_ID.equals(character.id())) {
            boolean handled = UndeadSkills.useSkill(player, slot, alternate);
            UndeadStateManager.syncToClient(player);
            return handled;
        }

        if (ModCharacters.LEX_NINJIA_ID.equals(character.id())) {
            boolean handled = LexNinjiaSkills.useSkill(player, slot, alternate);
            LexNinjiaStateManager.syncToClient(player);
            return handled;
        }

        if (ModCharacters.MORSE_ID.equals(character.id())) {
            boolean handled = MorseSkills.useSkill(player, slot, alternate);
            MorseStateManager.syncToClient(player);
            return handled;
        }

        if (ModCharacters.TOXIK_ID.equals(character.id())) {
            boolean handled = ToxikSkills.useSkill(player, slot, alternate);
            ToxikStateManager.syncToClient(player);
            return handled;
        }

        if (ModCharacters.RAPTOR_ID.equals(character.id())) {
            boolean handled = RaptorSkills.useSkill(player, slot, alternate);
            RaptorStateManager.syncToClient(player);
            return handled;
        }

        if (ModCharacters.VLINDER_ID.equals(character.id())) {
            boolean handled = VlinderSkills.useSkill(player, slot, alternate);
            VlinderStateManager.syncToClient(player);
            return handled;
        }

        if (ModCharacters.TEMPEST_ID.equals(character.id())) {
            boolean handled = TempestSkills.useSkill(player, slot, alternate);
            TempestStateManager.syncToClient(player);
            return handled;
        }

        player.displayClientMessage(
                Component.translatable("message.dealt_force_skills.skill_placeholder",
                        Component.translatable(skill.get().translationKey()),
                        Component.translatable(character.nameTranslationKey())),
                true
        );
        return true;
    }
}

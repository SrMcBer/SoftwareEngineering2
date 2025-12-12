// composables/useMedicationHelpers.ts
import { ref } from "vue";
import type { Medication } from "../types/business";

export function useMedicationHelpers() {
  const now = ref(new Date());

  function isMedicationCompleted(m: Medication): boolean {
    if (!m.endDate) {
      return false;
    }
    const end = new Date(m.endDate);
    const today = new Date();
    end.setHours(23, 59, 59, 999);
    return end.getTime() < today.getTime();
  }

  function isMedicationActive(m: Medication): boolean {
    return !isMedicationCompleted(m);
  }

  function getNextDoseMeta(m: Medication) {
    const nextDueAt = (m as any).nextDueAt as string | undefined | null;
    if (!nextDueAt) return null;

    const target = new Date(nextDueAt);
    if (isNaN(target.getTime())) return null;

    const diffMs = target.getTime() - now.value.getTime();
    const isOverdue = diffMs <= 0;
    const absMs = Math.abs(diffMs);

    const totalSeconds = Math.floor(absMs / 1000);
    const days = Math.floor(totalSeconds / (60 * 60 * 24));
    const hours = Math.floor((totalSeconds % (60 * 60 * 24)) / (60 * 60));
    const minutes = Math.floor((totalSeconds % (60 * 60)) / 60);
    const seconds = totalSeconds % 60;

    let core: string;
    if (days > 0) {
      core = `${days}d ${hours}h`;
    } else if (hours > 0) {
      core = `${hours}h ${minutes}m`;
    } else if (minutes > 0) {
      core = `${minutes}m ${seconds}s`;
    } else {
      core = `${seconds}s`;
    }

    const label = isOverdue ? `overdue by ${core}` : `in ${core}`;
    const isSoon = !isOverdue && absMs <= 60 * 60 * 1000;

    return { label, isOverdue, isSoon };
  }

  function getDoseProgress(m: Medication) {
    const last = (m as any).lastAdministeredAt as string | undefined | null;
    const next = (m as any).nextDueAt as string | undefined | null;

    if (!last || !next) return null;

    const lastDate = new Date(last);
    const nextDate = new Date(next);

    if (isNaN(lastDate.getTime()) || isNaN(nextDate.getTime())) return null;

    const totalMs = nextDate.getTime() - lastDate.getTime();
    if (totalMs <= 0) return null;

    const elapsedMs = now.value.getTime() - lastDate.getTime();
    const rawPercent = (elapsedMs / totalMs) * 100;
    const percent = Math.max(0, Math.min(100, rawPercent));

    const isOverdue = now.value.getTime() >= nextDate.getTime();

    return {
      percent,
      isOverdue,
      lastLabel: formatDoseDateTime(last),
      nextLabel: formatDoseDateTime(next),
    };
  }

  function formatDoseDateTime(iso: string): string {
    const d = new Date(iso);
    if (isNaN(d.getTime())) return iso;
    return d.toLocaleString();
  }

  function startClock() {
    const timer = window.setInterval(() => {
      now.value = new Date();
    }, 1000);
    return timer;
  }

  return {
    now,
    isMedicationCompleted,
    isMedicationActive,
    getNextDoseMeta,
    getDoseProgress,
    formatDoseDateTime,
    startClock,
  };
}

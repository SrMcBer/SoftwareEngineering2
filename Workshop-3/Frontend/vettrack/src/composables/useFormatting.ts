// composables/useFormatting.ts
import { computed } from "vue";
import type { Patient } from "../types/business";

export function useFormatting() {
  function formatVisitDate(s: string): string {
    const d = new Date(s);
    if (isNaN(d.getTime())) return s;
    return d.toLocaleDateString(undefined, {
      year: "numeric",
      month: "short",
      day: "numeric",
    });
  }

  function formatReminderDue(iso: string): string {
    const d = new Date(iso);
    if (isNaN(d.getTime())) return iso;
    return d.toLocaleString();
  }

  function speciesBadgeClass(species?: string | null): string {
    const s = (species ?? "").toLowerCase();

    if (s.includes("dog") || s.includes("canine"))
      return "bg-emerald-100 text-emerald-800";
    if (s.includes("cat") || s.includes("feline"))
      return "bg-sky-100 text-sky-800";
    if (s.includes("equine") || s.includes("horse"))
      return "bg-amber-100 text-amber-800";
    if (s.includes("bovine") || s.includes("cow"))
      return "bg-violet-100 text-violet-800";

    return "bg-slate-100 text-slate-800";
  }

  function sexBadgeClass(sex?: string | null): string {
    const s = (sex ?? "").toLowerCase();
    if (s === "f" || s.startsWith("hembra")) return "bg-pink-100 text-pink-800";
    if (s === "m" || s.startsWith("macho")) return "bg-blue-100 text-blue-800";
    return "bg-slate-100 text-slate-800";
  }

  function getAgeLabel(patient: Patient | null) {
    return computed(() => {
      const dob = patient?.dob;
      if (!dob) return null;
      const birth = new Date(dob);
      if (isNaN(birth.getTime())) return null;

      const now = new Date();
      let years = now.getFullYear() - birth.getFullYear();
      let months = now.getMonth() - birth.getMonth();
      if (months < 0) {
        years -= 1;
        months += 12;
      }
      if (years <= 0) return `${months} mo old`;
      if (months === 0) return `${years} years old`;
      return `${years} years ${months} mo old`;
    });
  }

  return {
    formatVisitDate,
    formatReminderDue,
    speciesBadgeClass,
    sexBadgeClass,
    getAgeLabel,
  };
}

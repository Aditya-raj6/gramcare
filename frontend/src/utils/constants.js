// Enumerations mirrored from the backend domain model.
export const opts = (values) => values.map((v) => ({ value: v, label: humanize(v) }));

const ACRONYMS = new Set(['OPD', 'PHC', 'CHC', 'NCD', 'TB', 'BMI', 'RO', 'ANM', 'ASHA', 'MPW', 'JJM', 'H2S', 'BP']);

export function humanize(value) {
  if (value === null || value === undefined) return '';
  return String(value)
    .split('_')
    .map((w) => (ACRONYMS.has(w.toUpperCase()) ? w.toUpperCase() : w.length ? w[0].toUpperCase() + w.slice(1).toLowerCase() : w))
    .join(' ')
    .replace('X Ray', 'X-Ray');
}

export const ROLES = ['ADMIN', 'DOCTOR', 'HEALTH_WORKER', 'PHARMACIST', 'SANITATION_OFFICER'];
export const PRIORITIES = ['LOW', 'MEDIUM', 'HIGH', 'CRITICAL'];
export const VILLAGES = ['Rampur', 'Sundarpur', 'Kishanganj', 'Devgarh', 'Bhimnagar', 'Lakshmipur'];

export const E = {
  gender: ['MALE', 'FEMALE', 'OTHER'],
  residentStatus: ['ACTIVE', 'MIGRATED', 'DECEASED'],
  bloodGroup: ['A+', 'A-', 'B+', 'B-', 'AB+', 'AB-', 'O+', 'O-'],
  recordType: ['DIAGNOSIS', 'LAB_RESULT', 'VACCINATION', 'PRESCRIPTION', 'ALLERGY', 'SURGERY', 'ANTENATAL', 'NOTE'],
  visitType: ['OPD', 'EMERGENCY', 'FOLLOW_UP', 'HOME_VISIT', 'HEALTH_CAMP', 'ANTENATAL', 'IMMUNIZATION'],
  centreType: ['SUB_CENTRE', 'PHC', 'CHC', 'DISPENSARY', 'WELLNESS_CENTRE'],
  centreStatus: ['OPERATIONAL', 'LIMITED', 'CLOSED'],
  facilityCategory: ['LABORATORY', 'PHARMACY', 'LABOUR_ROOM', 'X_RAY', 'AMBULANCE', 'INPATIENT_BEDS', 'VACCINATION_COLD_CHAIN',
    'OPERATION_THEATRE', 'TELEMEDICINE', 'DRINKING_WATER', 'POWER_BACKUP', 'TOILETS'],
  facilityStatus: ['AVAILABLE', 'UNDER_MAINTENANCE', 'UNAVAILABLE'],
  workerRole: ['DOCTOR', 'NURSE', 'ANM', 'ASHA', 'PHARMACIST', 'LAB_TECHNICIAN', 'MPW', 'SANITATION_WORKER', 'ADMIN_STAFF'],
  workerStatus: ['ACTIVE', 'ON_LEAVE', 'INACTIVE'],
  serviceCategory: ['GENERAL_OPD', 'MATERNAL_HEALTH', 'CHILD_HEALTH', 'IMMUNIZATION', 'FAMILY_PLANNING', 'COMMUNICABLE_DISEASE',
    'NCD', 'DIAGNOSTICS', 'EMERGENCY', 'DENTAL', 'EYE_CARE', 'MENTAL_HEALTH'],
  dayOfWeek: ['MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY'],
  appointmentType: ['GENERAL', 'ANTENATAL', 'IMMUNIZATION', 'FOLLOW_UP', 'CHRONIC_CARE', 'DENTAL', 'EYE', 'TELECONSULTATION'],
  appointmentStatus: ['SCHEDULED', 'CHECKED_IN', 'COMPLETED', 'CANCELLED', 'NO_SHOW'],
  appointmentPriority: ['ROUTINE', 'URGENT', 'EMERGENCY'],
  treatmentType: ['MEDICATION', 'PROCEDURE', 'LAB_TEST', 'THERAPY', 'REFERRAL', 'COUNSELLING'],
  itemCategory: ['MEDICINE', 'VACCINE', 'SUPPLY', 'EQUIPMENT', 'CONSUMABLE', 'DIAGNOSTIC_KIT'],
  dosageForm: ['TABLET', 'CAPSULE', 'SYRUP', 'INJECTION', 'OINTMENT', 'DROPS', 'SACHET', 'VIAL', 'UNIT', 'KIT'],
  requestStatus: ['PENDING', 'APPROVED', 'ORDERED', 'FULFILLED', 'REJECTED'],
  requestSource: ['MANUAL', 'AUTO_LOW_STOCK', 'FIELD_REPORT'],
  transactionType: ['RECEIPT', 'ISSUE', 'ADJUSTMENT', 'EXPIRED_WRITE_OFF', 'TRANSFER_OUT'],
  campType: ['GENERAL_CHECKUP', 'EYE_CARE', 'DENTAL', 'MATERNAL_CHILD_HEALTH', 'VACCINATION', 'NCD_SCREENING', 'TB_SCREENING',
    'BLOOD_DONATION', 'AWARENESS', 'MENTAL_HEALTH'],
  campStatus: ['PLANNED', 'ONGOING', 'COMPLETED', 'CANCELLED'],
  screeningType: ['BLOOD_PRESSURE', 'BLOOD_SUGAR', 'BMI', 'HAEMOGLOBIN', 'VISION', 'DENTAL', 'TB_SYMPTOMS', 'ORAL_CANCER',
    'BREAST_EXAM', 'CHILD_GROWTH', 'MALARIA'],
  screeningOutcome: ['NORMAL', 'ABNORMAL', 'CRITICAL'],
  followUpStatus: ['PENDING', 'IN_PROGRESS', 'COMPLETED', 'MISSED'],
  sanitationType: ['COMMUNITY_TOILET', 'PUBLIC_TOILET', 'SCHOOL_TOILET', 'DRAINAGE', 'WASTE_COLLECTION_POINT', 'SOAK_PIT',
    'HANDWASH_STATION', 'FAECAL_SLUDGE_PLANT', 'COMPOST_PIT'],
  cleanliness: ['CLEAN', 'SATISFACTORY', 'DIRTY', 'UNUSABLE'],
  operationalStatus: ['OPERATIONAL', 'UNDER_MAINTENANCE', 'NON_FUNCTIONAL'],
  maintenanceType: ['CLEANING', 'REPAIR', 'PLUMBING', 'ELECTRICAL', 'PEST_CONTROL', 'DESLUDGING', 'DRAIN_DESILTING', 'PAINTING', 'CONSTRUCTION'],
  taskStatus: ['PENDING', 'ASSIGNED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED'],
  taskSource: ['MANUAL', 'COMPLAINT', 'INSPECTION', 'FIELD_REPORT', 'SCHEDULED'],
  complaintCategory: ['UNCLEAN_TOILET', 'BLOCKED_DRAIN', 'NO_WATER', 'BROKEN_FIXTURE', 'SEWAGE_OVERFLOW', 'GARBAGE_NOT_COLLECTED',
    'OPEN_DEFECATION', 'BAD_ODOUR', 'NO_LIGHTING', 'OTHER'],
  complaintStatus: ['OPEN', 'IN_PROGRESS', 'RESOLVED', 'CLOSED', 'REJECTED'],
  waterSourceType: ['HANDPUMP', 'BOREWELL', 'OPEN_WELL', 'PIPED_SUPPLY', 'OVERHEAD_TANK', 'POND', 'RIVER', 'RO_PLANT', 'TANKER'],
  waterSourceStatus: ['FUNCTIONAL', 'PARTIALLY_FUNCTIONAL', 'NON_FUNCTIONAL', 'CONTAMINATED'],
  quality: ['SAFE', 'MARGINAL', 'UNSAFE', 'UNTESTED'],
  testMethod: ['FIELD_TEST_KIT', 'H2S_VIAL', 'LABORATORY'],
  issueCategory: ['WATER_CONTAMINATION', 'SUPPLY_DISRUPTION', 'PIPELINE_LEAKAGE', 'STAGNANT_WATER', 'MOSQUITO_BREEDING',
    'WATERBORNE_DISEASE_CLUSTER', 'OPEN_DEFECATION', 'SOLID_WASTE', 'OTHER'],
  issueStatus: ['OPEN', 'INVESTIGATING', 'ACTION_TAKEN', 'RESOLVED', 'CLOSED'],
  actionType: ['CHLORINATION', 'SOURCE_DISINFECTION', 'PIPELINE_REPAIR', 'HANDPUMP_REPAIR', 'FLUSHING', 'ALTERNATE_SUPPLY',
    'SOURCE_CLOSURE', 'FOGGING', 'LARVICIDE', 'AWARENESS_DRIVE', 'ORS_DISTRIBUTION', 'MEDICAL_CAMP', 'OTHER'],
  actionStatus: ['PLANNED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED'],
  reportItemType: ['SANITATION_MAINTENANCE', 'SANITATION_COMPLAINT', 'MEDICINE_SHORTAGE', 'WATER_ISSUE'],
};

/** Status → semantic tone, rendered by <StatusTag>. */
const TONES = {
  good: ['ACTIVE', 'OPERATIONAL', 'AVAILABLE', 'COMPLETED', 'FULFILLED', 'RESOLVED', 'CLOSED', 'CLEAN', 'SAFE', 'FUNCTIONAL',
    'IN_STOCK', 'NORMAL', 'ROUTED', 'CHECKED_IN', 'APPROVED'],
  info: ['SCHEDULED', 'PLANNED', 'ONGOING', 'IN_PROGRESS', 'ASSIGNED', 'ORDERED', 'INVESTIGATING', 'ACTION_TAKEN', 'SATISFACTORY',
    'LOW', 'ROUTINE'],
  warning: ['PENDING', 'OPEN', 'LIMITED', 'UNDER_MAINTENANCE', 'ON_LEAVE', 'LOW_STOCK', 'MARGINAL', 'DIRTY', 'ABNORMAL', 'MEDIUM',
    'PARTIALLY_FUNCTIONAL', 'URGENT', 'UNTESTED'],
  serious: ['HIGH', 'NO_SHOW', 'MISSED'],
  critical: ['CRITICAL', 'CLOSED_CENTRE', 'UNAVAILABLE', 'UNUSABLE', 'UNSAFE', 'CONTAMINATED', 'NON_FUNCTIONAL', 'OUT_OF_STOCK',
    'EXPIRED', 'FAILED', 'EMERGENCY', 'REJECTED', 'CANCELLED', 'INACTIVE', 'DECEASED'],
};

export function toneOf(status) {
  for (const [tone, list] of Object.entries(TONES)) if (list.includes(status)) return tone;
  return 'neutral';
}

/** Reference data-viz palette (validated categorical order + reserved status colours). */
export const SERIES = ['#2a78d6', '#eb6834', '#1baf7a', '#eda100', '#e87ba4', '#008300', '#6250d6', '#e34948'];
export const STATUS_COLORS = { good: '#0ca30c', warning: '#fab219', serious: '#ec835a', critical: '#d03b3b', info: '#2a78d6', neutral: '#8c8c8c' };
